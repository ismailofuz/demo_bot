package mike.com.demo_bot.bot;


public interface State {
    String START = "Entering name";
    String EMAIL = "Entering email";
    String POSITION = "Entering position";
    String PHONE_NUMBER = "Entering phone_number";
    String CHECK_DETAILS = "Checking details";
    String MENU = "Showing menu";
    String COMMENTING = "Comment writing";
    String SUBMIT_COMMENT = "Comment submission";
    String SETTING_TARGET = "Setting target";
    String SUBMIT_TARGET = "Submitting target";
    String SETTING_RESULT = "Setting result";
    String SUBMIT_RESULT = "Submit result";
    String SETTING_NEWS = "Setting news for all";
    String SUBMIT_NEWS = "Submitting news";

    String CHOOSE_ONE = "Choosing one user";
    String SEND_PRIVATE_MESSAGE = "Sending private message";
    String SUBMIT_PRIVATE_MESSAGE = "Submitting private message";

}
