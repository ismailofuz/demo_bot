package mike.com.demo_bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import mike.com.demo_bot.entity.BotUser;
import mike.com.demo_bot.entity.Messages;
import mike.com.demo_bot.repository.BotUserRepository;
import mike.com.demo_bot.repository.MessagesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Component("botcha")
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {


    // cridentials
    @Value("${telegram_bot_username}")
    String username;
    @Value("${telegram_bot_botToken}")
    String botToken;


    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    private final BotUserRepository botUserRepository;
    private final MessagesRepository messagesRepository;

    private final BotService service;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {


            //message :
            Message message = update.getMessage();
            String text = message.getText();
            Long chatId = message.getChatId();

            // bot action
            SendChatAction action = new SendChatAction();
            action.setAction(ActionType.TYPING);
            action.setChatId(chatId.toString());
            execute(action);


            //current user
            BotUser current_user;

            Optional<BotUser> byChatId = botUserRepository.findByChatId(chatId.toString());
            if (byChatId.isPresent()) {
                current_user = byChatId.get();
            } else {
                BotUser botUser = new BotUser();
                botUser.setChatId(chatId.toString());
                BotUser save = botUserRepository.save(botUser);
                current_user = save;
            }

            if (text != null && text.equals("/start")) {
//                SendMessage sendMessage = service.welcome(update);
                execute(service.welcome(update));
                current_user.setState(State.START);
                botUserRepository.save(current_user);
            } else {

                String state = current_user.getState();

                switch (state) {
                    case State.START:
                        // name keladi
                        execute(service.asking_email(update, current_user));
                        current_user.setState(State.EMAIL);
                        botUserRepository.save(current_user);
                        break;
                    case State.EMAIL:
                        // email keladi
                        execute(service.asking_position(update, current_user));
                        break;
                    case State.POSITION:
                        // position keladi nasib bo'lsa:
                        execute(service.asking_phone_number(update, current_user));
                        break;
                    case State.PHONE_NUMBER:
                        execute(service.checking_details(update, current_user));
                        break;
                    case State.CHECK_DETAILS:
                        execute(service.submission_details(update, current_user));
                        break;
                    case State.MENU:
                        switch (text) {
                            // user buttons
                            case Buttons.COMMENT:
                                execute(service.commenting(update, current_user));
                                break;
                            case Buttons.PROFILE:
                                execute(service.show_profile(update, current_user));
                                break;
                            case Buttons.WEEKLY_TARGET:
                                execute(service.setting_weekly_target(update, current_user));
                                break;
                            case Buttons.SETTING_WEEKLY_RESULTS:
                                execute(service.setting_result(update, current_user));
                                break;

                            // ADMIN PAGE :
                            case Buttons.ALL_PEOPLE:
                                // hamma odamlarni ko'rsatish : // inline beriladi nasib  bo'lsa
                                execute(service.show_all_people(update, current_user));
                                // direct message bo'ladi inshaallah
                                break;
                            case Buttons.ALL_TARGETS:
                                // shu va o'tgan haftada qo'yilgan rejalar haqida ma'lumot beradi nasib bo'lsa :
                                execute(service.current_Targets(update, current_user));
                                break;
                            case Buttons.INBOX:
                                // barcha xabarlarni ko'rsatish :
                                execute(service.inbox(update, current_user)); // nasib bo'lsa tayyor
                                break;
                            case Buttons.NEWS:
                                execute(service.news_button(update, current_user));
                                break;
                            case Buttons.SHOW_ALL_TARGET_HISTORY:
                                // nasib bo'lsa bu tayyor
                                execute(service.allTargets(update, current_user));
                                break;
                            default:
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setChatId(chatId.toString());
                                sendMessage.setText("Noto'g'ri ma'lumot kiritildi!");
                                execute(sendMessage);
                                break;
                        }
                        break;
                    case State.COMMENTING:
                        execute(service.committing_message(update, current_user));
                        break;
                    case State.SUBMIT_COMMENT:
                        execute(service.submit_comment(update, current_user));
                        break;
                    case State.SETTING_TARGET:
                        execute(service.setting_target(update, current_user));
                        break;
                    case State.SUBMIT_TARGET:
                        execute(service.submit_target(update, current_user));
                        break;
                    case State.SETTING_RESULT:
                        execute(service.settingResult(update, current_user));
                        break;
                    case State.SUBMIT_RESULT:
                        execute(service.submit_result(update, current_user));
                        break;
                    case State.SETTING_NEWS:
                        // xabar matni keladi nasib bo'lsa :
                        execute(service.settingNews(update, current_user));
                        break;
                    case State.SUBMIT_NEWS:
                        // hammaga nasib bo'lsa xabar boradi shuni shu yerdan boshqa joyda execute qip bo'lmedi :
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId.toString());
                        //  textda button bo'lishi kerak o'zi

                        if (text.equals(Buttons.ACCEPT)) {
                            // hatto o'ziga ham shu xabarni yuborishi kerak :
                            List<BotUser> all = botUserRepository.findAll();

                            // odam yubormoqchi bo'lgan xabarni op kelish kerak :
                            Optional<Messages> optionalMessages = messagesRepository.findBySender_IdAndIsSentIsFalse(current_user.getId());
                            if (optionalMessages.isPresent()) {
                                Messages messages = optionalMessages.get();

                                messages.setSent(true);
                                messagesRepository.save(messages);
                                for (BotUser user : all) {
                                    sendMessage.setChatId(user.getChatId());
                                    sendMessage.setText("\uD83D\uDD0A " + messages.getText() + "\n" +
                                            "Xabar yuborgan odam : " + current_user.getFullName());
                                    execute(sendMessage);
                                }

                                sendMessage = service.admin_menu(update, current_user);
                                sendMessage.setText("Sizning xabaringiz hammaga yuborildi!");
                                execute(sendMessage);
                            }


                        } else if (text.equals(Buttons.REJECT)) {
                            Optional<Messages> optionalMessage = messagesRepository.findBySender_IdAndIsSentIsFalse(current_user.getId());
                            if (optionalMessage.isPresent()) {
                                messagesRepository.delete(optionalMessage.get());
                            }

                            sendMessage = service.admin_menu(update, current_user);
                            sendMessage.setText("Menudan tanlashingiz mumkin");
                            execute(sendMessage);
                        } else {
                            sendMessage.setText("Iltimos tugmalardan birontasini bosing");
                            execute(sendMessage);
                        }
                        break;
                    case State.SEND_PRIVATE_MESSAGE:
                        execute(service.submit_private_message(update, current_user));
                        break;
                    case State.SUBMIT_PRIVATE_MESSAGE:
                        // xabar yuboradigan joyi bor shuning uchun shu yerda yoziladi code :
                        SendMessage sendMessage1 = new SendMessage();
                        sendMessage1.setChatId(chatId.toString());

                        Optional<Messages> optional = messagesRepository.findBySender_IdAndIsSentIsFalse(current_user.getId());
                        Messages messages = optional.get();

                        if (text.equals(Buttons.ACCEPT)) {
                            messages.setSent(true);
                            Messages save = messagesRepository.save(messages);

                            // o'sha userga xabar boradi nasib bo'lsa :
                            SendMessage privateMessage = new SendMessage();
                            privateMessage.setChatId(save.getReceiver().getChatId());
                            privateMessage.setText("\uD83D\uDD0A " + save.getText() + "\n" +
                                    "Yuborgan Xodim " + current_user.getFullName());
                            execute(privateMessage);

                            sendMessage1 = service.admin_menu(update,current_user);

                            sendMessage1.setText("Xabar yuborildi , Javob ni Xabarlar bo'limidan ko'ra olasiz");

                            execute(sendMessage1);
                        } else if (text.equals(Buttons.REJECT)) {
                            messagesRepository.delete(messages);

                            if (current_user.getRole().equals("admin")) {
                                sendMessage1 = service.admin_menu(update, current_user);
                            } else {
                                sendMessage1 = service.user_menu(update, current_user);
                            }
                            sendMessage1.setText("Menudan tanlang :");
                            execute(sendMessage1);

                        } else {
                            sendMessage1.setText("Iltimos bironta tugmani bosing!");
                            execute(sendMessage1);
                        }

                        break;
                    default:


                        if (current_user.getRole().equals("admin")) {
                            sendMessage = service.admin_menu(update, current_user);
                        } else {
                            sendMessage = service.user_menu(update, current_user);
                        }
                        sendMessage.setText("Noto'g'ri ma'lumot kiritildi!");
                        execute(sendMessage);

                }
            }


        } else if (update.hasCallbackQuery()) {

            BotUser current_user;
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Long chatId = callbackQuery.getMessage().getChatId();
            String data = callbackQuery.getData();

            Optional<BotUser> byChatId = botUserRepository.findByChatId(chatId.toString());
            if (byChatId.isPresent()) {
                current_user = byChatId.get();

                execute(service.send_private_message(update, current_user));
            } else {
                // shu yergacha keldan odam bo'ladi nasib bo'lsa
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId.toString());
                sendMessage.setText("User not found");
                execute(sendMessage);
            }
        }


    }
}
