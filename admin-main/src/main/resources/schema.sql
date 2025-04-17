-- Tạo bảng brands (Hãng laptop)
CREATE TABLE brands (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Tạo bảng laptops (Thông tin laptop)
CREATE TABLE laptops (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    discount DECIMAL(5, 2) DEFAULT 0,
    brand_id INT NOT NULL,
    specs NVARCHAR(MAX) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_laptops_brands FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT CHK_laptops_price CHECK (price > 0),
    CONSTRAINT CHK_laptops_discount CHECK (discount >= 0 AND discount <= 100),
    CONSTRAINT CHK_laptops_stock CHECK (stock_quantity >= 0)
);

-- Tạo bảng admins (Quản trị viên)
CREATE TABLE admins (
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    role NVARCHAR(20) NOT NULL DEFAULT 'STAFF',
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT CHK_admins_role CHECK (role IN ('ADMIN', 'STAFF'))
);

-- Tạo bảng logs (Lịch sử thao tác)
CREATE TABLE logs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    action NVARCHAR(50) NOT NULL,
    admin_id INT,
    time DATETIME DEFAULT GETDATE(),
    details NVARCHAR(MAX),
    CONSTRAINT FK_logs_admins FOREIGN KEY (admin_id) REFERENCES admins(id)
);

-- Tạo VIEW hiển thị thông tin laptop với tên hãng
CREATE VIEW vw_laptop_details AS
SELECT 
    l.id,
    l.name,
    l.price,
    l.discount,
    (l.price * (1 - l.discount/100)) AS discounted_price,
    b.name AS brand_name,
    l.specs,
    l.stock_quantity,
    l.created_at,
    l.updated_at
FROM laptops l
JOIN brands b ON l.brand_id = b.id;

-- Tạo VIEW thống kê laptop theo hãng
CREATE VIEW vw_laptop_statistics AS
SELECT 
    b.name AS brand_name,
    COUNT(l.id) AS laptop_count,
    AVG(l.price) AS average_price,
    MIN(l.price) AS min_price,
    MAX(l.price) AS max_price,
    SUM(l.stock_quantity) AS total_stock
FROM brands b
LEFT JOIN laptops l ON b.id = l.brand_id
GROUP BY b.name;

-- Tạo TRIGGER ghi log khi thêm laptop mới
CREATE TRIGGER trg_laptop_insert
ON laptops
AFTER INSERT
AS
BEGIN
    DECLARE @laptop_id INT, @laptop_name NVARCHAR(255), @details NVARCHAR(MAX)
    
    SELECT @laptop_id = id, @laptop_name = name FROM inserted
    
    SET @details = 'Thêm laptop mới: ' + @laptop_name + ' (ID: ' + CAST(@laptop_id AS NVARCHAR) + ')'
    
    INSERT INTO logs (action, details)
    VALUES ('INSERT_LAPTOP', @details)
END;

-- Tạo TRIGGER ghi log khi cập nhật laptop
CREATE TRIGGER trg_laptop_update
ON laptops
AFTER UPDATE
AS
BEGIN
    DECLARE @laptop_id INT, @laptop_name NVARCHAR(255), @details NVARCHAR(MAX)
    
    SELECT @laptop_id = id, @laptop_name = name FROM inserted
    
    SET @details = 'Cập nhật laptop: ' + @laptop_name + ' (ID: ' + CAST(@laptop_id AS NVARCHAR) + ')'
    
    INSERT INTO logs (action, details)
    VALUES ('UPDATE_LAPTOP', @details)
END;

-- Tạo TRIGGER ghi log khi xóa laptop
CREATE TRIGGER trg_laptop_delete
ON laptops
AFTER DELETE
AS
BEGIN
    DECLARE @laptop_id INT, @laptop_name NVARCHAR(255), @details NVARCHAR(MAX)
    
    SELECT @laptop_id = id, @laptop_name = name FROM deleted
    
    SET @details = 'Xóa laptop: ' + @laptop_name + ' (ID: ' + CAST(@laptop_id AS NVARCHAR) + ')'
    
    INSERT INTO logs (action, details)
    VALUES ('DELETE_LAPTOP', @details)
END;

-- Tạo STORED PROCEDURE thêm laptop mới
CREATE PROCEDURE sp_add_laptop
    @name NVARCHAR(255),
    @price DECIMAL(15, 2),
    @discount DECIMAL(5, 2),
    @brand_id INT,
    @specs NVARCHAR(MAX),
    @stock_quantity INT,
    @admin_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Thêm laptop mới
        INSERT INTO laptops (name, price, discount, brand_id, specs, stock_quantity)
        VALUES (@name, @price, @discount, @brand_id, @specs, @stock_quantity);
        
        DECLARE @laptop_id INT = SCOPE_IDENTITY();
        
        -- Cập nhật log với admin_id
        UPDATE logs
        SET admin_id = @admin_id
        WHERE action = 'INSERT_LAPTOP' AND 
              details LIKE '%' + @name + ' (ID: ' + CAST(@laptop_id AS NVARCHAR) + ')%' AND
              admin_id IS NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;

-- Tạo STORED PROCEDURE cập nhật laptop
CREATE PROCEDURE sp_update_laptop
    @id INT,
    @name NVARCHAR(255),
    @price DECIMAL(15, 2),
    @discount DECIMAL(5, 2),
    @brand_id INT,
    @specs NVARCHAR(MAX),
    @stock_quantity INT,
    @admin_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Cập nhật thông tin laptop
        UPDATE laptops
        SET name = @name,
            price = @price,
            discount = @discount,
            brand_id = @brand_id,
            specs = @specs,
            stock_quantity = @stock_quantity,
            updated_at = GETDATE()
        WHERE id = @id;
        
        -- Cập nhật log với admin_id
        UPDATE logs
        SET admin_id = @admin_id
        WHERE action = 'UPDATE_LAPTOP' AND 
              details LIKE '%' + @name + ' (ID: ' + CAST(@id AS NVARCHAR) + ')%' AND
              admin_id IS NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;

-- Tạo STORED PROCEDURE xóa laptop
CREATE PROCEDURE sp_delete_laptop
    @id INT,
    @admin_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Lấy thông tin laptop trước khi xóa
        DECLARE @laptop_name NVARCHAR(255);
        SELECT @laptop_name = name FROM laptops WHERE id = @id;
        
        -- Xóa laptop
        DELETE FROM laptops WHERE id = @id;
        
        -- Cập nhật log với admin_id
        UPDATE logs
        SET admin_id = @admin_id
        WHERE action = 'DELETE_LAPTOP' AND 
              details LIKE '%' + @laptop_name + ' (ID: ' + CAST(@id AS NVARCHAR) + ')%' AND
              admin_id IS NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;

-- Tạo STORED PROCEDURE cập nhật giảm giá hàng loạt theo hãng
CREATE PROCEDURE sp_update_discount_by_brand
    @brand_id INT,
    @discount DECIMAL(5, 2),
    @admin_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Cập nhật giảm giá cho tất cả laptop của hãng
        UPDATE laptops
        SET discount = @discount,
            updated_at = GETDATE()
        WHERE brand_id = @brand_id;
        
        -- Thêm log
        DECLARE @brand_name NVARCHAR(100), @details NVARCHAR(MAX);
        SELECT @brand_name = name FROM brands WHERE id = @brand_id;
        
        SET @details = 'Cập nhật giảm giá hàng loạt cho hãng ' + @brand_name + ' thành ' + CAST(@discount AS NVARCHAR) + '%';
        
        INSERT INTO logs (action, admin_id, details)
        VALUES ('BATCH_UPDATE_DISCOUNT', @admin_id, @details);
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;

-- Tạo FUNCTION tính giá sau khi giảm giá
CREATE FUNCTION fn_calculate_discounted_price (@price DECIMAL(15, 2), @discount DECIMAL(5, 2))
RETURNS DECIMAL(15, 2)
AS
BEGIN
    RETURN @price * (1 - @discount/100);
END;

-- Thêm dữ liệu mẫu cho bảng admins
INSERT INTO admins (full_name, email, username, password_hash, role)
VALUES (N'Administrator', 'admin@laptopshop.com', 'admin', '$2a$10$rPiEAgQNIT1TCoQWDDPrDeGDQV3Q0T6RBlzwJ0q/NlWUepZ1ZYQdC', 'ADMIN'); -- password: admin123

-- Thêm dữ liệu mẫu cho bảng brands
INSERT INTO brands (name) VALUES 
('Dell'),
('HP'),
('Lenovo'),
('Asus'),
('Acer'),
('Apple'),
('MSI');

-- Thêm dữ liệu mẫu cho bảng laptops
INSERT INTO laptops (name, price, discount, brand_id, specs, stock_quantity) VALUES
('Dell XPS 13', 25000000, 5, 1, N'{"cpu":"Intel Core i7-1165G7","ram":"16GB","storage":"512GB SSD","display":"13.4 inch FHD+","gpu":"Intel Iris Xe"}', 10),
('HP Spectre x360', 28000000, 8, 2, N'{"cpu":"Intel Core i7-1165G7","ram":"16GB","storage":"1TB SSD","display":"13.3 inch 4K OLED","gpu":"Intel Iris Xe"}', 8),
('Lenovo ThinkPad X1 Carbon', 32000000, 10, 3, N'{"cpu":"Intel Core i7-1165G7","ram":"16GB","storage":"1TB SSD","display":"14 inch 4K","gpu":"Intel Iris Xe"}', 5),
('Asus ROG Zephyrus G14', 35000000, 7, 4, N'{"cpu":"AMD Ryzen 9 5900HS","ram":"32GB","storage":"1TB SSD","display":"14 inch QHD","gpu":"NVIDIA RTX 3060"}', 12),
('Acer Predator Helios 300', 29000000, 12, 5, N'{"cpu":"Intel Core i7-11800H","ram":"16GB","storage":"512GB SSD","display":"15.6 inch FHD 144Hz","gpu":"NVIDIA RTX 3070"}', 15),
('Apple MacBook Pro 14', 45000000, 0, 6, N'{"cpu":"Apple M1 Pro","ram":"16GB","storage":"512GB SSD","display":"14.2 inch Liquid Retina XDR","gpu":"14-core GPU"}', 7),
('MSI GS66 Stealth', 38000000, 5, 7, N'{"cpu":"Intel Core i9-11900H","ram":"32GB","storage":"1TB SSD","display":"15.6 inch FHD 360Hz","gpu":"NVIDIA RTX 3080"}', 9);