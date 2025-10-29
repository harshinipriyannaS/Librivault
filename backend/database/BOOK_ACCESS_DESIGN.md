# LibriVault Book Access Design

## Corrected Approach: Single PDF with Permission-Based Access

You were absolutely right to question the dual URI approach. The design has been simplified and corrected to follow a more logical and efficient pattern.

## How It Works

### 1. **Single S3 URI Storage**
- Each book has **only ONE** S3 URI pointing to the complete PDF file
- No separate preview files are stored
- Simpler storage management and reduced S3 costs

### 2. **Permission-Based Access Control**

#### **Preview Access (Public)**
- **Anyone** can request a preview URL for any book
- Backend generates a secure S3 URL with 2-hour expiration
- **Frontend responsibility**: Limit display to first 5 pages only
- No server-side PDF processing required

#### **Full Book Access (Restricted)**
- **Only users with active borrow records** can access full book
- **Admin and Librarian roles** have unrestricted access
- Backend validates permissions before generating secure URLs
- Same PDF file, but frontend shows all pages

### 3. **Access Flow**

```
Book Request → Permission Check → URL Generation → Frontend Display Control
```

#### **For Preview:**
1. User clicks "Preview" on any book
2. Backend generates secure URL (no permission check)
3. Frontend receives URL and displays first 5 pages only
4. URL expires in 2 hours

#### **For Full Book:**
1. User clicks "Read Full Book"
2. Backend checks if user has borrowed the book
3. If authorized: generates secure URL for full access
4. Frontend receives URL and displays all pages
5. URL expires in 2 hours

## Database Schema (Corrected)

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    description TEXT,
    category_id BIGINT NOT NULL,
    s3_uri VARCHAR(500),              -- Single PDF file
    cover_image_uri VARCHAR(500),     -- Book cover image
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    published_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Removed Fields:**
- ~~`preview_s3_uri`~~ - No longer needed

## API Endpoints

### Preview Access (Public)
```
GET /api/books/{id}/preview
Response: {
  "bookId": 1,
  "previewUrl": "https://s3.amazonaws.com/...",
  "expiresIn": "2 hours",
  "message": "Preview URL generated successfully"
}
```

### Full Book Access (Authenticated)
```
GET /api/books/{id}/access
Headers: Authorization: Bearer <jwt-token>
Response: {
  "bookId": 1,
  "secureUrl": "https://s3.amazonaws.com/...",
  "expiresIn": "2 hours",
  "message": "Secure access URL generated successfully"
}
```

## Frontend Implementation

### Preview Display
```javascript
// Fetch preview URL (no auth required)
const previewResponse = await fetch(`/api/books/${bookId}/preview`);
const { previewUrl } = await previewResponse.json();

// Display PDF with page limit
<PDFViewer 
  url={previewUrl} 
  maxPages={5}           // Limit to first 5 pages
  watermark="Preview"    // Optional preview watermark
/>
```

### Full Book Display
```javascript
// Fetch full book URL (auth required)
const fullBookResponse = await fetch(`/api/books/${bookId}/access`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { secureUrl } = await fullBookResponse.json();

// Display full PDF
<PDFViewer 
  url={secureUrl} 
  maxPages={null}        // No page limit
/>
```

## Security Features

1. **Time-Limited URLs**: All S3 URLs expire in 2 hours
2. **Permission Validation**: Server-side checks before URL generation
3. **JWT Authentication**: Required for full book access
4. **Role-Based Access**: Admin/Librarian bypass borrowing requirements
5. **Audit Logging**: All access attempts are logged

## Benefits of This Approach

1. **Simplified Storage**: One file per book instead of two
2. **Cost Effective**: Reduced S3 storage costs
3. **Easier Management**: Single file upload/update process
4. **Better Security**: Permission-based rather than file-based access
5. **Frontend Control**: Page limiting handled by client
6. **Scalable**: No server-side PDF processing required

## Sample Data

The database now includes sample books with realistic S3 URIs:

```sql
INSERT INTO books (title, author, s3_uri, ...) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 's3://librivault-book-storage/books/the-great-gatsby.pdf', ...),
('Clean Code', 'Robert C. Martin', 's3://librivault-book-storage/books/clean-code.pdf', ...);
```

## Migration Notes

If you have existing books with separate preview files:
1. Keep the main PDF files
2. Delete preview files from S3 to save costs
3. Update database to remove `preview_s3_uri` references
4. Frontend will automatically limit preview display

This corrected approach is much cleaner, more efficient, and easier to maintain while providing the same user experience.