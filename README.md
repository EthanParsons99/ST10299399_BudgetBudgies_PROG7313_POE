![Image](https://github.com/user-attachments/assets/41970f9d-9b05-4999-ac14-2b0898d77e33)
# Budget Budgies - Budget Tracker for Androids
This repository is for the sign of a moblie app that allows users to keep track of their money. The app allows you to 
manage your money in a stress free way and more engaging.

## Contributers 
- [@EthanParsons99](https://github.com/EthanParsons99) - Ethan Parsons: ST10299399
- [@ST10303017](https://github.com/ST10303017) - Calwyn Govender: ST10303017
- [@Joshcybr](https://github.com/Joshcybr) - Joshua Thomas: ST10263292
- [@ST10400684](https://github.com/ST10400684) - Morne Erasmu: ST10400684


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
- Automated Testing

## Technology Used
- Language: Java/Kotlin
- IDE: Android Studio Meerkat 2024.3.1
- Database: RoomDB
- Version Control: GitHub
- Device: Pixel 9 Pro API 35

## Addional Notes
- Youtube video Link has been added in the pdf which is uploaded on ARC
- GitHub Link is also added in the pdf
- Some features are still a work in progress {WIP} which need to be implemented in part 3
- Gamification still needs adding to make the app more entertaining
- Top Navigation bars back and menu buttons are trickey to click but they do work
- Camera is able to take photo on emulator but you are not able to open the photo after the transaction was added
- But when you run it on a physical device the camera takes a photo and after the transaction was added you are able to view the photo

## Upcoming Features
- Connection to a Firbase database
- Working Analytics page for a detailed view of how money is being spent and earned
- Working progress dashboard to keep track of achievements and see where you can improve saving
- Working Achievements page to add some gamification to saving money and completing goals
- More gamification to goals page to make it more engaging
- Feature to allow users to change currency
- Implementation of innovative features to make app diffrent then others
- Overall improvements to make app smoother and nicer to use

## How to run Project
1. Make sure to have Android Studio installed on your PC and open it
2. Copy the repository link
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
- User – stores the data when a user signs up
- Account – stores the accounts that the user added
- Category – keeps track of categories that a user created
- Expense - keeps track of the expenses that a user makes
- Income - keeps track of the incomes the user adds
- Goal - keeps track of any budget goals the user creates
- Each entity is annotated with @Entity, and the DAO interfaces to access the data

## References
- Icons: https://uxwing.com/
- Camera Integration: https://youtu.be/Pi4m5_JTBj0?si=98jUqAeQRwRn47v8
- Spinners/Dropdown: https://developer.android.com/develop/ui/views/components/spinner
                     and https://youtu.be/zIhfp_qftes?si=6FmLU7O_uzLaxnBD
- Category Design: https://youtu.be/saKrGCWlJDs?si=cO3yjADXNrJLkDvh
- Layouts: https://developer.android.com/develop/ui/views/layout/declaring-layout
- Creating Expense Entry: https://projectgurukul.org/expense-tracker-android-app-source-code/
- Database Configuration: https://code.nkslearning.com/blogs/a-beginner-guide-to-using-and-setting-up-room-database-in-android-with-java_65745c4ce501fcefe61e
- Database Help: https://youtu.be/bOd3wO0uFr8?si=Vw6kC3hRLHf40gDu
- ImageViews: https://stackoverflow.com/questions/15414676/freely-move-imageview-around-page
- GradientBackground: https://youtu.be/sE0-f9e_aaA?si=Hil4NxJsqlMJtTZv
- ChatGPT Gradient Help: https://youtu.be/sE0-f9e_aaA?si=Hil4NxJsqlMJtTZv
- Parcelables: https://medium.com/@calren24/android-parcelables-made-easy-acb742bcf96b
- IIE Module Manual: Programming 3C/Open Source Coding(Introduction) PROG7313
- RecyclerViews: https://blog.mindorks.com/android-recyclerview-in-kotlin/
- ReadME help: https://github.com/othneildrew/Best-README-Template?tab=readme-ov-file and https://youtu.be/qIaWozjDyPk?si=TfGOWV3QQYPY7U_I
