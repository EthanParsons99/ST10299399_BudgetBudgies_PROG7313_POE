Budget Budgies - Budget Tracker for Androids

This repository is for the sign of a moblie app that allows users to keep track of their money. The app allows you to 
manage your money in a stress free way and more engaging.

Contributers 
Ethan Parsons
Calwyn Govender
Morne Erasmus 
Joshua Thomas

About the project
Budget Budgies is a mobile app to make it easy for users to keep track of their money. Users can add accounts manually and log expense and incomes and choose which account it should be from. There is no risk involved with using the app as you never have to connect your actual bank account.  The app uses a simple design so that anybody who wants to start saving and keep track of their money can use it. 

Key Features
•	Sign up / Login
•	Adding bank accounts
•	Logging income and expenses
•	Creation of categories
•	Displaying total budget left
•	User can view previous months expenses and incomes


Technology Used
Language: Java/Kotlin
IDE: Android Studio Meerkat 2024.3.1
Git version:
Database: RoomDB
Version Control: GitHub
Device: Pixel 9 Pro API 35


How to run Project
1.	Clone the repository
2.	Open Android Studio
3.	Build the project 
•	Wait for the Gradle sync to finish before running the app
4.	Run the app
•	Make sure that you have a device connected

Database setup
•	User – stores the data when a user signs up
•	Account – stores the accounts that the user added
•	Category – keeps track of categories that a user created
•	Expense - keeps track of the expenses that a user makes
•	Income - keeps track of the incomes the user adds
Each entity is annotated with @Entity, and the DAO interfaces to access the data

Example Code
User Entity
 
UserDao
 
References
