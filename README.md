# Library Manager Bot

Helps users manage and interact with a library system through intuitive interface on Telegram. This bot uses a local MongoDB database for storing and retrieving information.

Find it [here](t.me/myminilibraryBot).


## Features

- **Find**: Search for books by title, author, or view all available books in the library. 
- **Add new**: Add new books by providing a title and author.
- **Take/return**: Borrow and become a holder of a book or return and mark it as available in the library. 
- **View holder**: View the current holder of the book.



## Structure
The project consists of the following files:

- **Bot**: The main class that handles updates and interactions with the Telegram API.
- **Keyboard**: Provides methods to generate the various types of keyboards (main menu, inline keyboards for managing).
- **Library**: Handles interactions with the MongoDB database to store and retrieve book details.
- **BotState**: Enum class to manage bot states.
