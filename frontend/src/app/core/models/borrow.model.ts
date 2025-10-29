export interface BorrowRequest {
  id: number;
  readerId: number;
  readerName: string;
  readerEmail: string;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  categoryName: string;
  status: RequestStatus;
  requestedAt: string;
  reviewedAt?: string;
  reviewedByName?: string;
  reviewNotes?: string;
}

export interface BorrowRecord {
  id: number;
  readerId: number;
  readerName: string;
  readerEmail: string;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  categoryName: string;
  borrowedAt: string;
  dueDate: string;
  returnedAt?: string;
  status: BorrowStatus;
  usedCredit: boolean;
  creditsEarned: number;
}

export interface Fine {
  id: number;
  readerId: number;
  readerName: string;
  readerEmail: string;
  borrowRecordId: number;
  bookTitle: string;
  bookAuthor: string;
  amount: number;
  overdueDays: number;
  status: FineStatus;
  description: string;
  createdAt: string;
  paidAt?: string;
  waivedAt?: string;
  waivedByName?: string;
  waiverReason?: string;
}

export enum RequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  DECLINED = 'DECLINED'
}

export enum BorrowStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE'
}

export enum FineStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  WAIVED = 'WAIVED'
}

export interface BorrowStats {
  totalActiveBorrows: number;
  totalOverdueBooks: number;
  totalOutstandingFines: number;
  mostActiveReaders: ActiveReader[];
}

export interface ActiveReader {
  readerId: number;
  readerName: string;
  borrowCount: number;
}

export interface CreateBorrowRequestRequest {
  bookId: number;
}

export interface ReviewBorrowRequestRequest {
  notes?: string;
}

export interface WaiveFineRequest {
  reason: string;
}