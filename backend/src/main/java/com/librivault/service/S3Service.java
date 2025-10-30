package com.librivault.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.librivault.entity.Book;
import com.librivault.entity.BorrowRecord;
import com.librivault.entity.User;
import com.librivault.entity.enums.Role;
import com.librivault.repository.BorrowRecordRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    @Autowired(required = false)
    private S3Client s3Client;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region}")
    private String region;
    
    public String uploadBookFile(MultipartFile file, String bookTitle) {
        try {
            String fileName = generateFileName(file.getOriginalFilename(), bookTitle);
            String key = "books/" + fileName;
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String s3Uri = String.format("s3://%s/%s", bucketName, key);
            logger.info("Book file uploaded successfully: {}", s3Uri);
            
            return s3Uri;
        } catch (IOException e) {
            logger.error("Failed to upload book file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload book file", e);
        }
    }
    

    
    public String uploadCoverImage(MultipartFile file, String bookTitle) {
        try {
            String fileName = generateFileName(file.getOriginalFilename(), bookTitle + "_cover");
            String key = "covers/" + fileName;
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String s3Uri = String.format("s3://%s/%s", bucketName, key);
            logger.info("Cover image uploaded successfully: {}", s3Uri);
            
            return s3Uri;
        } catch (IOException e) {
            logger.error("Failed to upload cover image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload cover image", e);
        }
    }
    
    public String generateSecureDownloadUrl(String s3Uri, Duration expiration) {
        try {
            String key = extractKeyFromS3Uri(s3Uri);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            try (S3Presigner presigner = S3Presigner.create()) {
                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                String url = presignedRequest.url().toString();
                
                logger.debug("Generated secure download URL for: {}", key);
                return url;
            }
        } catch (Exception e) {
            logger.error("Failed to generate secure download URL for: {}", s3Uri, e);
            throw new RuntimeException("Failed to generate secure download URL", e);
        }
    }
    
    public String generatePreviewUrl(String s3Uri) {
        // Preview URLs have shorter expiration since they're limited to first 5 pages
        return generateSecureDownloadUrl(s3Uri, Duration.ofHours(2));
    }
    
    public String generateFullBookUrl(String s3Uri) {
        // Full book URLs should have shorter expiration for security
        return generateSecureDownloadUrl(s3Uri, Duration.ofHours(2));
    }
    
    public void deleteFile(String s3Uri) {
        try {
            String key = extractKeyFromS3Uri(s3Uri);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully: {}", s3Uri);
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", s3Uri, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
    
    public boolean fileExists(String s3Uri) {
        try {
            String key = extractKeyFromS3Uri(s3Uri);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", s3Uri, e);
            return false;
        }
    }
    
    public long getFileSize(String s3Uri) {
        try {
            String key = extractKeyFromS3Uri(s3Uri);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (Exception e) {
            logger.error("Failed to get file size: {}", s3Uri, e);
            return 0;
        }
    }
    
    private String generateFileName(String originalFileName, String bookTitle) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String sanitizedTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "_");
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%d%s", sanitizedTitle, uniqueId, System.currentTimeMillis(), extension);
    }
    
    private String extractKeyFromS3Uri(String s3Uri) {
        if (s3Uri == null || !s3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URI: " + s3Uri);
        }
        
        // Remove s3://bucket-name/ prefix
        String withoutProtocol = s3Uri.substring(5); // Remove "s3://"
        int firstSlashIndex = withoutProtocol.indexOf('/');
        
        if (firstSlashIndex == -1) {
            throw new IllegalArgumentException("Invalid S3 URI format: " + s3Uri);
        }
        
        return withoutProtocol.substring(firstSlashIndex + 1);
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getRegion() {
        return region;
    }
    
    // Secure access methods based on user permissions
    public String getSecureBookUrl(Book book, User user) {
        try {
            // Check if user has permission to access the full book
            if (canUserAccessFullBook(book, user)) {
                logger.info("Generating full book access URL for user: {} and book: {}", user.getId(), book.getId());
                return generateFullBookUrl(book.getS3Uri());
            } else {
                logger.info("User {} does not have permission to access full book: {}", user.getId(), book.getId());
                throw new RuntimeException("Access denied: You need to borrow this book to read the full content");
            }
        } catch (Exception e) {
            logger.error("Failed to generate secure book URL for user: {} and book: {}", user.getId(), book.getId(), e);
            throw new RuntimeException("Failed to generate book access URL", e);
        }
    }
    
    public String getPreviewUrl(Book book) {
        try {
            if (book.getS3Uri() != null && !book.getS3Uri().isEmpty()) {
                logger.info("Generating preview URL for book: {} (first 5 pages only)", book.getId());
                // Same PDF file, but frontend will limit to first 5 pages for preview
                return generatePreviewUrl(book.getS3Uri());
            } else {
                logger.warn("No PDF file available for book: {}", book.getId());
                throw new RuntimeException("Book PDF not available for preview");
            }
        } catch (Exception e) {
            logger.error("Failed to generate preview URL for book: {}", book.getId(), e);
            throw new RuntimeException("Failed to generate preview URL", e);
        }
    }
    
    public boolean canUserAccessFullBook(Book book, User user) {
        try {
            // Admin and Librarian can access all books
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN) {
                logger.debug("Admin/Librarian access granted for book: {}", book.getId());
                return true;
            }
            
            // Check if user has an active borrow record for this book
            BorrowRecord activeBorrow = borrowRecordRepository
                .findByReaderAndBookAndReturnedAtIsNull(user, book)
                .orElse(null);
            
            if (activeBorrow != null) {
                // Check if the borrow is not overdue (optional - you might want to allow access even if overdue)
                LocalDateTime now = LocalDateTime.now();
                boolean isNotOverdue = activeBorrow.getDueDate().isAfter(now);
                
                if (isNotOverdue) {
                    logger.debug("Active borrow record found for user: {} and book: {}", user.getId(), book.getId());
                    return true;
                } else {
                    logger.debug("Borrow record is overdue for user: {} and book: {}", user.getId(), book.getId());
                    // You can decide whether to allow access to overdue books or not
                    // For now, we'll allow access but log it
                    return true;
                }
            }
            
            logger.debug("No active borrow record found for user: {} and book: {}", user.getId(), book.getId());
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking book access permission for user: {} and book: {}", user.getId(), book.getId(), e);
            return false;
        }
    }
    
    public String generateTimeLimitedBookUrl(Book book, User user, Duration customDuration) {
        try {
            if (canUserAccessFullBook(book, user)) {
                logger.info("Generating time-limited book URL for user: {} and book: {} with duration: {}", 
                           user.getId(), book.getId(), customDuration);
                return generateSecureDownloadUrl(book.getS3Uri(), customDuration);
            } else {
                throw new RuntimeException("Access denied: You need to borrow this book to read the full content");
            }
        } catch (Exception e) {
            logger.error("Failed to generate time-limited book URL for user: {} and book: {}", user.getId(), book.getId(), e);
            throw new RuntimeException("Failed to generate book access URL", e);
        }
    }
    
    public String getCoverImageUrl(Book book) {
        try {
            if (book.getCoverImageUri() != null && !book.getCoverImageUri().isEmpty()) {
                logger.debug("Generating cover image URL for book: {}", book.getId());
                // Cover images can have longer expiration as they're public-facing
                return generateSecureDownloadUrl(book.getCoverImageUri(), Duration.ofDays(1));
            } else {
                logger.debug("No cover image available for book: {}", book.getId());
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to generate cover image URL for book: {}", book.getId(), e);
            return null;
        }
    }
    
    // Method to validate and refresh expired URLs
    public String refreshBookUrl(Book book, User user) {
        try {
            if (canUserAccessFullBook(book, user)) {
                logger.info("Refreshing book URL for user: {} and book: {}", user.getId(), book.getId());
                // Generate a new URL with fresh expiration
                return generateFullBookUrl(book.getS3Uri());
            } else {
                throw new RuntimeException("Access denied: Your borrowing permission has expired");
            }
        } catch (Exception e) {
            logger.error("Failed to refresh book URL for user: {} and book: {}", user.getId(), book.getId(), e);
            throw new RuntimeException("Failed to refresh book access URL", e);
        }
    }
    
    // Method to check if a book file exists and is accessible
    public boolean isBookAccessible(Book book) {
        try {
            if (book.getS3Uri() == null || book.getS3Uri().isEmpty()) {
                logger.warn("Book {} has no S3 URI", book.getId());
                return false;
            }
            
            boolean exists = fileExists(book.getS3Uri());
            logger.debug("Book {} accessibility check: {}", book.getId(), exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Error checking book accessibility for book: {}", book.getId(), e);
            return false;
        }
    }
    
    // Method to get book metadata without providing access
    public BookFileMetadata getBookMetadata(Book book) {
        try {
            if (book.getS3Uri() == null || book.getS3Uri().isEmpty()) {
                return null;
            }
            
            long fileSize = getFileSize(book.getS3Uri());
            boolean exists = fileExists(book.getS3Uri());
            
            return new BookFileMetadata(book.getId(), fileSize, exists);
            
        } catch (Exception e) {
            logger.error("Failed to get book metadata for book: {}", book.getId(), e);
            return null;
        }
    }
    
    // Inner class for book metadata
    public static class BookFileMetadata {
        private final Long bookId;
        private final long fileSize;
        private final boolean exists;
        
        public BookFileMetadata(Long bookId, long fileSize, boolean exists) {
            this.bookId = bookId;
            this.fileSize = fileSize;
            this.exists = exists;
        }
        
        public Long getBookId() { return bookId; }
        public long getFileSize() { return fileSize; }
        public boolean exists() { return exists; }
        
        public String getFormattedFileSize() {
            if (fileSize < 1024) return fileSize + " B";
            if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
            if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}