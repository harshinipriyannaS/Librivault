# LibriVault Database Setup

This directory contains database initialization files for the LibriVault Digital Library system.

## Files

- `setup.sql` - Complete database setup script with tables and initial data
- `../src/main/resources/schema.sql` - Spring Boot schema initialization (tables only)
- `../src/main/resources/data.sql` - Spring Boot data initialization (sample data)

## Quick Setup

### Option 1: Manual Database Setup (Recommended for Development)

1. **Create MySQL Database:**
   ```bash
   mysql -u root -p
   ```

2. **Run the setup script:**
   ```sql
   source /path/to/backend/database/setup.sql
   ```

   Or import via command line:
   ```bash
   mysql -u root -p < backend/database/setup.sql
   ```

### Option 2: Spring Boot Auto-Initialization

The application is configured to automatically create tables and insert data when started:

1. **Configure database connection in `application.properties`:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/librivault_db?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

2. **Start the Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```

   The application will automatically:
   - Create the database if it doesn't exist
   - Create all tables using `schema.sql`
   - Insert initial data using `data.sql`

## Database Schema

### Tables Created:
- `users` - System users (Admin, Librarian, Reader)
- `librarians` - Librarian-specific information
- `categories` - Book categories
- `books` - Book catalog
- `subscriptions` - User subscription plans
- `payments` - Payment records (Stripe integration)
- `borrow_requests` - Book borrow requests
- `borrow_records` - Active/completed borrows
- `fines` - Overdue book fines
- `notifications` - User notifications

## Default Users

The system comes with pre-configured users for testing:

### Admin Account
- **Email:** admin@librivault.com
- **Password:** admin123
- **Role:** ADMIN
- **Access:** Full system administration

### Librarian Accounts
- **Email:** librarian1@librivault.com
- **Password:** librarian123
- **Role:** LIBRARIAN
- **Department:** Fiction & Literature

- **Email:** librarian2@librivault.com
- **Password:** librarian123
- **Role:** LIBRARIAN
- **Department:** Science & Technology

### Reader Accounts
- **Email:** reader1@librivault.com
- **Password:** reader123
- **Role:** READER
- **Credits:** 2

- **Email:** reader2@librivault.com
- **Password:** reader123
- **Role:** READER
- **Subscription:** Premium

- **Email:** reader3@librivault.com
- **Password:** reader123
- **Role:** READER
- **Credits:** 1

## Sample Data

The database includes:
- 8 book categories
- 8 sample books
- Default subscriptions for all users
- Sample borrow requests and records
- Welcome notifications

## Configuration

### Environment Variables

Set these environment variables for production:

```bash
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your-super-secret-jwt-key

# AWS S3
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your-s3-bucket-name
AWS_REGION=your-aws-region

# Stripe
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Email
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### Application Properties

The `application.properties` file is configured with sensible defaults:

- **Database:** MySQL with automatic table creation
- **JPA:** Hibernate with `update` DDL mode
- **Security:** JWT-based authentication
- **File Storage:** AWS S3 integration
- **Payments:** Stripe integration
- **Email:** SMTP configuration

## Troubleshooting

### Common Issues:

1. **Database Connection Failed:**
   - Ensure MySQL is running
   - Check username/password in application.properties
   - Verify database exists or `createDatabaseIfNotExist=true` is set

2. **Tables Not Created:**
   - Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
   - Ensure schema.sql is in `src/main/resources/`
   - Check for SQL syntax errors in logs

3. **Initial Data Not Inserted:**
   - Ensure data.sql is in `src/main/resources/`
   - Check for foreign key constraint errors
   - Verify `spring.sql.init.mode=always` if needed

4. **Password Hash Issues:**
   - Default passwords are bcrypt hashed
   - Use BCryptPasswordEncoder for new passwords
   - Hash: `$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a`

### Reset Database:

To completely reset the database:

```sql
DROP DATABASE librivault_db;
source backend/database/setup.sql
```

## Production Considerations

1. **Change Default Passwords:** Update all default user passwords
2. **Environment Variables:** Use secure environment variables
3. **Database Backup:** Implement regular backup strategy
4. **SSL/TLS:** Enable SSL for database connections
5. **Monitoring:** Set up database monitoring and logging
6. **Performance:** Add appropriate indexes for large datasets
7. **Security:** Restrict database access and use strong passwords