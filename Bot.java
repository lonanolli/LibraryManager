package LibraryBot_v1;

import org.bson.Document;
import java.util.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot{

    private Library db = new Library();
    public BotState state = BotState.DEFAULT;
    String setTitle = "", setAuthor = "";

    @Override
    public String getBotUsername() {
        return "LibraryManagerBot";
    }

    @Override
    public String getBotToken() {
        // insert your token here
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatIdLong = update.getMessage().getChatId();
            String chatId = String.valueOf(chatIdLong);
            handleTextMessage(update, chatId, text);
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatIdLong = update.getCallbackQuery().getMessage().getChatId();
            String chatId = String.valueOf(chatIdLong);
            handleCallback(update, chatId, callbackData);

        }
    }

    // help function for sending simple texts
    private void sendMessage(String chatId, String text){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // main menu builder
    private void sendMainMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("See the menu below and choose an option:");
        message.setReplyMarkup(Keyboard.createMainMenu());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // books keyboard builder
    private void showBooks(String chatId, List<Document> books) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose a book:");
        message.setReplyMarkup(Keyboard.createBookKeyboard(books));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // find keyboard builder
    private void sendFindBookMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose an option:");
        message.setReplyMarkup(Keyboard.createFindKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // take keyboard builder
    private void sendTakeBookMenu(String chatId, Document book) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose an option:");
        message.setReplyMarkup(Keyboard.createTakeKeyboard(book));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // return keyboard builder
    private void sendReturnBookMenu(String chatId, String name){
        state = BotState.RETURN;
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose a book that you want to return:");
        var books = db.findByHolder(name);
        message.setReplyMarkup(Keyboard.createBookKeyboard(books));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e);
        }
    }

    // asks to enter title and author of a new book
    private void addBook(String chatId){
        sendMessage(chatId, "Please enter the book title:");
        state = BotState.ADD_AUTHOR;
    }


    // handles different messages (part of onUpdateReceived)
    private void handleTextMessage(Update update, String chatId, String text){
        switch (text) {
            case "/start":
                sendMainMenu(chatId);
                break;
            case "Add a new book":
                addBook(chatId);
                break;
            case "Find a book":
                sendFindBookMenu(chatId);
                break;
            case "Return a book":
                sendReturnBookMenu(chatId, update.getMessage().getFrom().getFirstName());
                break;
            default:
                handleTextMessageWithState(update, chatId, text);
        }
    }


    // help function for handleTextMessages (part of onUpdateReceived)
    private void handleTextMessageWithState(Update update, String chatId, String text){
        switch (state) {
            case FIND_BY_TITLE:
                List<Document> books_title = db.findByTitle(text);
                if (books_title.isEmpty()) {
                    sendMessage(chatId, "No books found with the title: " + text);
                    sendMainMenu(chatId);
                } else {
                    showBooks(chatId, books_title);
                }
                state = BotState.DEFAULT;
                break;

            case FIND_BY_AUTHOR:
                List<Document> books_author = db.findByAuthor(text);
                if (books_author.isEmpty()) {
                    sendMessage(chatId, "No books found by the author: " + text);
                    sendMainMenu(chatId);
                } else {
                    showBooks(chatId, books_author);
                }
                state = BotState.DEFAULT;
                break;

            case ADD_AUTHOR:
                setTitle = text;
                state = BotState.ADD_TITLE;
                sendMessage(chatId, "Please enter the book author:");
                break;

            case ADD_TITLE:
                setAuthor = text;
                state = BotState.DEFAULT;
                if (db.addBook(setTitle, setAuthor)) {
                    sendMessage(chatId, "Book was added successfully!");
                } else {
                    sendMessage(chatId, "Sorry, an error occurred.");
                }
                setTitle = "";
                setAuthor = "";
                break;

            default:
                sendMessage(chatId, "Unknown action. Please use the menu.");
        }
    }


    // handles clicking on buttons (part of onUpdateReceived)
    private void handleCallback(Update update, String chatId, String callbackData) {
        String text = callbackData.substring(6);
        callbackData = callbackData.substring(0,6);
        List<Document> books = db.findByTitle(text);

        switch (callbackData) {
            case "all:::":
                List<Document> allBooks = db.getAllBooks();
                showBooks(chatId, allBooks);
                break;
            case "title:":
                state = BotState.FIND_BY_TITLE;
                sendMessage(chatId, "Please enter the book title you are interested in");
                break;
            case "author":
                state = BotState.FIND_BY_AUTHOR;
                sendMessage(chatId, "Please enter the author you are interested in");
                break;
            case "TAKEEE":
                if (!books.isEmpty()) {
                    Document book = books.getFirst();
                    takeButtonClick(update, chatId, book);
                } else {
                    sendMessage(chatId, "Sorry, an error occurred.");
                    sendMainMenu(chatId);
                }
            case "back::":
                state = BotState.DEFAULT;
                sendMainMenu(chatId);
                break;
            case "HOLDER":
                sendMessage(chatId, "This book is " + text);
                sendMainMenu(chatId);
                break;
            case "BOOK::":
                if (!books.isEmpty()) {
                    Document book = books.getFirst();
                    bookButtonClick(chatId, book);
                } else {
                    sendMessage(chatId, "No books found with the given details.");
                    sendMainMenu(chatId);
                }
                break;

            default:
                sendMessage(chatId, callbackData + "(Callback)Unknown action. Please use the menu.");
                sendMainMenu(chatId);
        }
    }


    // help function for handleCallback, handles clicking on a book button
    private void bookButtonClick(String chatId,Document book){
        if (state == BotState.RETURN) {
            state = BotState.DEFAULT;
            sendMessage(chatId, book.get("title") + " should be returned to library");
            String name = "at library";
            db.updateHolder(name, book);
            sendMainMenu(chatId);

        } else {
            String text1 = "You selected: " + book.get("title") + " by " + book.get("author");
            sendMessage(chatId, text1);
            sendTakeBookMenu(chatId, book);
        }
    }

    // help function for handleCallback, handles clicking on a "take home" button
    private void takeButtonClick(Update update, String chatId, Document book){
        String holder = (String) book.get("holder");
        if (holder.startsWith("at")) {
            sendMessage(chatId, book.get("title") +" is now assigned to you!");
            String name = "held by " + update.getCallbackQuery().getFrom().getFirstName();
            db.updateHolder(name, book);
        } else if (holder.startsWith("held")) {
            sendMessage(chatId, "Unfortunately, " + book.get("title") + " is " + holder);
        } else {
            sendMessage(chatId, "Sorry, I have no information about the holder");
        }
    }


}
