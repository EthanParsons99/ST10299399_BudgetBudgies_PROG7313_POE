# Budget Budgies - Budget Tracker for Androids
This repository is for the sign of a moblie app that allows users to keep track of their money. The app allows you to 
manage your money in a stress free way and more engaging.

## Contributers 
- [@EthanParsons99](https://github.com/EthanParsons99) - Ethan Parsons
- [@ST10303017](https://github.com/ST10303017) - Calwyn Govender
- [@Joshcybr](https://github.com/Joshcybr) - Joshua Thomas
- [@ST10400684](https://github.com/ST10400684) - Morne Erasmus


## About the project
Budget Budgies is a mobile app to make it easy for users to keep track of their money in an easy way. The app allow users to sign up and login for the app. After they have successfully logged in users will be able to add bank accounts manually and log expense and incomes. When adding a transaction for an income or expense users are able to choose the bank account which they added. The app allows you to create your own categories for transactions. Users are also able to add saving goals they would like to achieve on the goals page. With this app there is no risk involved with using it as you never have to connect your actual bank account. The app uses a simple design for everyone to understand and use.

## Key Features
- Sign up / Login
- Adding bank accounts
- Creating new categories
- Logging income and expenses
- Adding reciept fotos for expenses
- User can create saving goals
- User can view previous months expenses and incomes
- Uses a LocalDB called RoomDB to store data

## Technology Used
Language: Java/Kotlin
IDE: Android Studio Meerkat 2024.3.1
Database: RoomDB
Version Control: GitHub
Device: Pixel 9 Pro API 35

## Addional Notes
- Video Link for to see how project works: 

## How to run Project
1. Make sure to have Android Studio installed on your PC and open it
2. Copy the repository link: https://github.com/EthanParsons99/BudgetBudgies_PROG7313_POE.git 
3. In Android Studio click on "Get from VCS" on the home screen
4. Or if you have an open project go to File > New > Project from Version Control
5. Paste the repository link in the URL field
6. Choose where to save the project and click Clone
7. Wait for all the gradle syncs to complete (this may take some time)
8. Android Studio could ask you to
   - Update the Gradle plugin: click update
   - Install missing SDKs: click install
9. Ensure that the correct device is connected for the emulator
10. Click run launch the project

## Database setup
•	User – stores the data when a user signs up
•	Account – stores the accounts that the user added
•	Category – keeps track of categories that a user created
•	Expense - keeps track of the expenses that a user makes
•	Income - keeps track of the incomes the user adds
Each entity is annotated with @Entity, and the DAO interfaces to access the data

## References
