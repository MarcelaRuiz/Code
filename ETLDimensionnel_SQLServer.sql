/*
Step 1 : creation de la base DW
*/


use master
go

IF  EXISTS (SELECT name FROM sys.databases WHERE name = N'StarNorthwind')
  BEGIN
    ALTER DATABASE [StarNorthwind] SET SINGLE_USER WITH ROLLBACK IMMEDIATE
    DROP DATABASE [StarNorthwind]
  END
GO

CREATE DATABASE [StarNorthwind] ON PRIMARY 
( NAME = N'StarNorthwind'
, FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL13.MSSQLSERVER\MSSQL\DATA\StarNorthwind.mdf' )
 LOG ON 
( NAME = N'StarNorthwind_log'
, FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL13.MSSQLSERVER\MSSQL\DATA\StarNorthwind_log.LDF' )
GO

use [StarNorthwind]
go
 
 /*
Partie Backup et Restore
*/
BACKUP DATABASE [StarNorthwind] 
TO  DISK = 
N'C:\temp\backups\StarNorthwind_BeforeETL.bak'
GO

/*
Creation des tables dimensions
*/

use StarNorthwind
go

CREATE TABLE dbo.DimDates (
  [DateKey] int NOT NULL PRIMARY KEY IDENTITY
, [Date] date NOT NULL  -- valeur de type date stockée dans la table
, [DateName] nVarchar(50)  -- nom du jour
, [Month] int NOT NULL -- numéro de mois de l'année
, [MonthName] nVarchar(50) NOT NULL -- nom du mois de l'année
, [Quarter] int NOT NULL --le numéro du trimestre (1,2,3 ou 4)
, [QuarterName] nVarchar(50) NOT NULL -- un nom de trimestre obtenu en concaténant plusieurs info
, [Year] int NOT NULL -- l'année numérique
, [YearName] nVarchar(50) NOT NULL  -- info sur l'année (en chaine de caractères)
)
go

create table dbo.DimProducts(
ProductKey int not null primary key identity,
ProductId int not null,
ProductName nvarchar(40) not null,
CategoryName nvarchar(15) not null,
"Description" ntext
)
go

create table dbo.DimCustomers(
CustomerKey int not null primary key identity,
CustomerId nvarchar(10) not null,
ContactName nvarchar(30),
Phone nvarchar(24),
CompanyName nvarchar(40) not null,
"Address" nvarchar(60),
City nvarchar(15),
PostalCode nvarchar(10),
Country nvarchar(15)
)
go

create table dbo.DimSuppliers(
SupplierKey int not null primary key identity,
SupplierId int not null,
ContactName nvarchar(30),
Phone nvarchar(24),
"Address" nvarchar(60),
City nvarchar(15),
PostalCode nvarchar(10),
Country nvarchar(15)
)
go

create table dbo.DimShippers(
ShipperKey int not null primary key identity,
ShipperId int not null,
CompanyName nvarchar(40) not null,
Phone nvarchar(25)
)
go

create table dbo.DimEmployees(
EmployeeKey int not null primary key identity,
EmployeeId int not null,
LastName nvarchar(20) not null,
FirstName nvarchar(10) not null,
BirthDate datetime,
HireDate datetime,
HomePhone nvarchar(24),
"Address" nvarchar(60),
City nvarchar(15),
PostalCode nvarchar(10),
Country nvarchar(15)
)
go

/*
Creation de la table des faits 
*/

use StarNorthwind
go

create table dbo.FactSales(
CustomerKey int not null, 
EmployeeKey int not null,
ProductKey int not null,
SupplierKey int not null, 
ShipperKey int not null,
OrderDateKey int not null, 
ShipperDateKey int not null, 
RequiredDateKey int not null, 
Freight money,
UnitPrice money not null,
Quantity smallint not null,
Discount real not null,
TotalSales real not null,
constraint [PK_FactSales] primary key clustered (CustomerKey,EmployeeKey,ProductKey,SupplierKey,ShipperKey,OrderDateKey,
ShipperDateKey,RequiredDateKey)
)
go

/*
Creation des clés étrangeres et des contraintes sur toutes les tables
*/

use StarNorthwind
go

Alter Table FactSales With Check Add Constraint FK_FactSales_DimProducts Foreign Key(ProductKey) References DimProducts(Productkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimCustomers Foreign Key(CustomerKey) References DimCustomers(Customerkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimSuppliers Foreign Key(SupplierKey) References DimSuppliers(SupplierKey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimShippers Foreign Key(ShipperKey) References DimShippers(Shipperkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimEmployees Foreign Key(EmployeeKey) References DimEmployees(EmployeeKey);
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesOrder Foreign Key(OrderDateKey) References DimDates(DateKey); 
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesShipper Foreign Key(ShipperDateKey) References DimDates(DateKey); 
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesRequired Foreign Key(RequiredDateKey) References DimDates(DateKey); 

/*
Partie ETL
Step 1: Drop constraints
*/

use StarNorthwind
go

Alter Table FactSales Drop Constraint FK_FactSales_DimProducts;
Alter Table FactSales Drop Constraint FK_FactSales_DimCustomers 
Alter Table FactSales Drop Constraint FK_FactSales_DimSuppliers;
Alter Table FactSales Drop Constraint FK_FactSales_DimShippers;
Alter Table FactSales Drop Constraint FK_FactSales_DimEmployees;
Alter Table FactSales  Drop Constraint FK_FactSales_DimDatesOrder; 
Alter Table FactSales  Drop Constraint FK_FactSales_DimDatesShipper; 
Alter Table FactSales  Drop Constraint FK_FactSales_DimDatesRequired; 
go

/*
Step 2: Truncate tables
*/

truncate table dbo.FactSales
truncate table dbo.Dimcustomers
truncate table dbo.DimEmployees
truncate table dbo.DimProducts
truncate table dbo.DimShippers
truncate table dbo.DimSuppliers
truncate table dbo.DimDates
go

/*
Step 3: Insert into tables
*/

use [StarNorthwind]
go

insert into [dbo].[DimCustomers]
Select 
CustomerId =c.[CustomerID],
ContactName = c.[ContactName],
Phone = c.[Phone],
CompanyName = c.[CompanyName],
"Address" = c.[Address],
City = UPPER(c.[City]),
PostalCode = UPPER(c.[PostalCode]),
Country = UPPER(c.[Country])
from [northwind].[dbo].[Customers] c
go


use [StarNorthwind]
go

insert into [dbo].[DimProducts]
select 
ProductId = p.[ProductID],
ProductName = p.[ProductName],
CategoryName = c.[CategoryName],
"Description" =c.[Description]
from [northwind].[dbo].[Products] p join [northwind].[dbo].[Categories] c on p.[CategoryID]=c.[CategoryID]
go

use [StarNorthwind]
go

insert into [dbo].[DimSuppliers]
select 
SupplierId = s.[SupplierID],
ContactName = s.[ContactName],
Phone = s.[Phone],
"Address" =s.[Address],
City = UPPER(s.[City]),
PostalCode = UPPER(s.[PostalCode]),
Country = UPPER(s.[Country])
from [northwind].[dbo].[Suppliers] s
go

use [StarNorthwind]
go

insert into [dbo].[DimShippers]
select 
ShipperId = [ShipperID],
CompanyName =[CompanyName],
Phone = cast([Phone] as nvarchar(25))
from [northwind].[dbo].[Shippers] sh
go

use [StarNorthwind]
go

insert into [dbo].[DimEmployees]
select 
EmployeeId = [EmployeeID],
LastName = [LastName],
FirstName = [FirstName],
BirthDate = [BirthDate],
HireDate = [HireDate],
HomePhone =[HomePhone],
"Address" = [Address],
City = UPPER([City]),
PostalCode = UPPER([PostalCode]),
Country = UPPER([Country])
from [northwind].[dbo].[Employees]
go

Use StarNorthwind
Go
Declare @StartDate datetime = '01/01/1996'
Declare @EndDate datetime = '01/01/1999' 

Declare @DateInProcess datetime
Set @DateInProcess = @StartDate

While @DateInProcess <= @EndDate
 Begin
 Insert Into DimDates 
 ( [Date], [DateName], [Month], [MonthName], [Quarter], [QuarterName], [Year], [YearName] )
 Values ( 
  @DateInProcess -- [Date]
  , DateName( weekday, @DateInProcess )  -- [DateName]  
  , Month( @DateInProcess ) -- [Month]   
  , DateName( month, @DateInProcess ) -- [MonthName]
  , DateName( quarter, @DateInProcess ) -- [Quarter]
  , 'Q' + DateName( quarter, @DateInProcess ) + ' - ' + Cast( Year(@DateInProcess) as nVarchar(50) ) -- [QuarterName] 
  , Year( @DateInProcess )
  , Cast( Year(@DateInProcess ) as nVarchar(50) ) -- [YearName] 
  )  
Set @DateInProcess = DateAdd(d, 1, @DateInProcess)
End
Set Identity_Insert [dbo].[DimDates] On
Insert Into [dbo].[DimDates] 
  ( [DateKey]
  , [Date]
  , [DateName]
  , [Month]
  , [MonthName]
  , [Quarter]
  , [QuarterName]
  , [Year], [YearName] )
  Select 
    [DateKey] = -1
  , [Date] =  Cast('01/01/1900' as nVarchar(50) )
  , [DateName] = Cast('Unknown Day' as nVarchar(50) )
  , [Month] = -1
  , [MonthName] = Cast('Unknown Month' as nVarchar(50) )
  , [Quarter] =  -1
  , [QuarterName] = Cast('Unknown Quarter' as nVarchar(50) )
  , [Year] = -1
  , [YearName] = Cast('Unknown Year' as nVarchar(50) )
  Union
  Select 
    [DateKey] = -2
  , [Date] = Cast('02/01/1900' as nVarchar(50) )
  , [DateName] = Cast('Corrupt Day' as nVarchar(50) )
  , [Month] = -2
  , [MonthName] = Cast('Corrupt Month' as nVarchar(50) )
  , [Quarter] =  -2
  , [QuarterName] = Cast('Corrupt Quarter' as nVarchar(50) )
  , [Year] = -2
  , [YearName] = Cast('Corrupt Year' as nVarchar(50) )
Go
  Set Identity_Insert [dbo].[DimDates] Off
Go

Use StarNorthwind
Go

insert into [dbo].[FactSales]
select 
CustomerKey = c.[CustomerKey],
EmployeeKey = e.[EmployeeKey],
ProductKey = p.[ProductKey],
SupplierKey = s.[SupplierKey], 
ShipperKey = sh.[ShipperKey],
OrderDateKey = da1.[DateKey], 
ShipperDateKey = da2.[DateKey], 
RequiredDateKey = da3.[DateKey], 
Freight = o.[Freight],
UnitPrice = do.[UnitPrice],
Quantity = do.[Quantity],
Discount = do.[Discount],
TotalSales = do.[Quantity] * do.[UnitPrice] * (1-do.[Discount])

from [northwind].[dbo].[Orders] o 
join [northwind].[dbo].[Order Details] do on o.OrderID = do.OrderID
join [northwind].[dbo].[Products] po on do.ProductID= po.[ProductID]
join [dbo].[DimSuppliers] s on s.SupplierId = po.SupplierID
join [dbo].[DimShippers] sh on sh.ShipperId = o.ShipVia
join [dbo].[DimDates] da1 on da1.Date=o.OrderDate 
join [dbo].[DimDates] da2 on da2.Date=o.ShippedDate
join [dbo].[DimDates] da3 on da3.Date=o.RequiredDate 
join [dbo].[DimProducts] p on p.ProductId= do.ProductID
join [dbo].[DimCustomers] c on o.CustomerID COLLATE DATABASE_DEFAULT = c.CustomerId COLLATE DATABASE_DEFAULT
join [dbo].[DimEmployees] e on o.EmployeeID = e.EmployeeId
go


/*
Recreate les clés étrangeres et des contraintes sur toutes les tables
*/

use StarNorthwind
go

Alter Table FactSales With Check Add Constraint FK_FactSales_DimProducts Foreign Key(ProductKey) References DimProducts(Productkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimCustomers Foreign Key(CustomerKey) References DimCustomers(Customerkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimSuppliers Foreign Key(SupplierKey) References DimSuppliers(SupplierKey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimShippers Foreign Key(ShipperKey) References DimShippers(Shipperkey);
Alter Table FactSales With Check Add Constraint FK_FactSales_DimEmployees Foreign Key(EmployeeKey) References DimEmployees(EmployeeKey);
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesOrder Foreign Key(OrderDateKey) References DimDates(DateKey); 
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesShipper Foreign Key(ShipperDateKey) References DimDates(DateKey); 
Alter Table FactSales  With Check Add Constraint FK_FactSales_DimDatesRequired Foreign Key(RequiredDateKey) References DimDates(DateKey); 


