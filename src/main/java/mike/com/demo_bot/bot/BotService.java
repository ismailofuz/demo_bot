package mike.com.demo_bot.bot;

import lombok.RequiredArgsConstructor;
import mike.com.demo_bot.entity.BotUser;
import mike.com.demo_bot.entity.DefaultUsers;
import mike.com.demo_bot.entity.Messages;
import mike.com.demo_bot.entity.Target;
import mike.com.demo_bot.entity.constants.MessageType;
import mike.com.demo_bot.repository.BotUserRepository;
import mike.com.demo_bot.repository.DefaultUsersRepository;
import mike.com.demo_bot.repository.MessagesRepository;
import mike.com.demo_bot.repository.TargetRepository;
import org.hibernate.query.criteria.internal.expression.function.CurrentTimestampFunction;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BotService {
    // repositories :
    private final BotUserRepository userRepository;


    public SendMessage welcome(Update update) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        sendMessage.setText("Botga xush kelibsiz! Botdan foydalanishda qo'llanishini " +
                "istagan username ni kiriting!");


        return sendMessage;
    }

    public SendMessage asking_email(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();

        Message message = update.getMessage();
        // username of user
        String text = message.getText();
        Long chatId = message.getChatId();

        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text + ", endi qo'shimcha ma'lumotlar uchun email jo'nating \n" +
                "ex : gmail@gmail.com");

        current_user.setFullName(text);
        userRepository.save(current_user);

        return sendMessage;
    }

    public SendMessage asking_position(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        // email
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (!text.startsWith("@") && (text.contains("@gmail") || text.contains("@mail"))) {
            current_user.setEmail(text);
            current_user.setState(State.POSITION);
            userRepository.save(current_user);
            sendMessage.setText("✅ Email tasdiqlandi  ,Siz kompaniyada qanday lavozimda ishlaysiz ?");
        } else {
            sendMessage.setText("⛔  Email tasdiqlanmadi! \n" +
                    "🔁 qaytadan urinib ko'ring");
        }

        return sendMessage;

    }

    public SendMessage asking_phone_number(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        // position
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));


        current_user.setPosition(text);
        current_user.setState(State.PHONE_NUMBER);
        userRepository.save(current_user);

        sendMessage.setText("✅ Lavozim muvaffaqiyatli o'rnatildi!\n" +
                "Siz bilan bog'lanishimiz uchun endi telefon raqamingizni quyidagi tugmani bosish oraqali tasdiqlang");


        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("☎️ Telefon raqam ulashish");
        button.setRequestContact(true);
        row.add(button);
        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public SendMessage checking_details(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        // contact keladi nasib bo'lsa

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (message.hasContact()) {
            Contact contact = message.getContact();
            String phoneNumber = contact.getPhoneNumber();
            current_user.setPhoneNumber(phoneNumber);
            current_user.setState(State.CHECK_DETAILS);
            userRepository.save(current_user);
            sendMessage.setText("Ma'lumotlarizni qayta ishlashga ruhsat beraszmi ?\n\n" +
                    "\uD83D\uDC64  " + current_user.getFullName() + " | " + current_user.getPosition() + " | " + current_user.getPhoneNumber());
            // buttons :

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            replyKeyboardMarkup.setSelective(true);

            List<KeyboardRow> rowList = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
            KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


            row.add(button);
            row.add(button1);
            rowList.add(row);


            replyKeyboardMarkup.setKeyboard(rowList);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

        } else {
            sendMessage.setText("Iltimos kontaktingizni ulashing!");
        }
        return sendMessage;
    }

    private final DefaultUsersRepository defaultUsers;

    public SendMessage submission_details(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        // email
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (text.equals(Buttons.ACCEPT)) {
            current_user.setState(State.MENU);
            userRepository.save(current_user);
            // keyin keladigan har bir ma'lumot nasib bo'lsa menuda tutib olamiza
            // user va adminga ajratish :
            Optional<DefaultUsers> byPhone_number = defaultUsers.findDefaultUsersByPhoneNumber(current_user.getPhoneNumber());

            if (byPhone_number.isPresent()) {
                current_user.setRole("admin");

                sendMessage = admin_menu(update, current_user);


            } else {
                current_user.setRole("user");

                sendMessage = user_menu(update, current_user);


            }
        } else if (text.equals(Buttons.REJECT)) {
            // qaytadan ma'lumot kiritishni boshlash
            sendMessage.setText("Iltimos ismingizni kiriting");
            current_user.setState(State.START);
            userRepository.save(current_user);

        } else {
            // bironta tugmani bosishini so'rash :
            sendMessage.setText("Noto'g'ri ma'lumot kirtildi!\n tugma bosilmadi!");

        }
        userRepository.save(current_user);
        return sendMessage;
    }

    protected SendMessage user_menu(Update update, BotUser currentUser) {

        // har bir xabar menuga boradi :
        currentUser.setState(State.MENU);
        userRepository.save(currentUser);

        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        sendMessage.setText("Menudan birontasini tanlang!");
        // user page
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> rowList = new ArrayList<>();


        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Buttons.WEEKLY_TARGET));
        row1.add(new KeyboardButton(Buttons.PROFILE));
        rowList.add(row1);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(Buttons.COMMENT));
        rowList.add(row);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(Buttons.SETTING_WEEKLY_RESULTS));
        rowList.add(keyboardRow);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    protected SendMessage admin_menu(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        current_user.setState(State.MENU);
        userRepository.save(current_user);

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        sendMessage.setText("Xush kelibsiz admin," + current_user.getFullName() +
                "! \nMenudan birontasini tanlang !");
        // admin page:
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row0 = new KeyboardRow();
        row0.add(new KeyboardButton(Buttons.ALL_TARGETS));
        row0.add(new KeyboardButton(Buttons.INBOX));
        rowList.add(row0);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(Buttons.ALL_PEOPLE));
        row.add(new KeyboardButton(Buttons.NEWS));
        rowList.add(row);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Buttons.WEEKLY_TARGET));
        row1.add(new KeyboardButton(Buttons.PROFILE));
        rowList.add(row1);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(Buttons.SHOW_ALL_TARGET_HISTORY));
        rowList.add(row3);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage commenting(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        current_user.setState(State.COMMENTING);
        userRepository.save(current_user);

        sendMessage.setText("HR ga yubormoqchi bo'lgan xabaringizni kiriting : ");

        // agar button qo'shib yubormasam avvalgi buttonnlar ko'rinib turadi
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();

//         button:
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.BACK);
        row.add(button);
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    private final MessagesRepository messagesRepository;

    public SendMessage committing_message(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (text.equals(Buttons.BACK)) {
            sendMessage = user_menu(update, current_user);
            return sendMessage;
        }

        // comment keldi shuni nasib bo'lsa saqlab hali lekin jo'natmasligim kerak hrga
        Messages messages = new Messages();
        messages.setText(text);
        messages.setSender(current_user);
        messages.setType(MessageType.COMMENT);

        messagesRepository.save(messages);

        sendMessage.setText("Ushbu xabarni HR ga jo'natishga rozimisz ?\n\n" +
                "Message :" + text);

        // state control:
        current_user.setState(State.SUBMIT_COMMENT);
        userRepository.save(current_user);


        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
        KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


        row.add(button);
        row.add(button1);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage submit_comment(Update update, BotUser current_user) {

        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        String button = message.getText();// button keladi nasib bo'lsa
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        Optional<Messages> optionalMessages = messagesRepository.findBySender_IdAndIsSentIsFalse(current_user.getId());

        if (button.equals(Buttons.ACCEPT)) {
            sendMessage = user_menu(update, current_user);// state control
            if (optionalMessages.isPresent()) {
                Messages messages = optionalMessages.get();
                messages.setSent(true);
                messagesRepository.save(messages);
                sendMessage.setText("Your message is sent to HR");
            } else {
                sendMessage.setText("malumotlar topilmadi bunaqa bo'lishi mumkin emas");
            }

        } else if (button.equals(Buttons.REJECT)) {
            Messages messages = optionalMessages.get();

            messagesRepository.delete(messages);
            sendMessage = user_menu(update, current_user);
        } else {
            sendMessage.setText("Iltimos bironta tugmani bosing!");
        }


        return sendMessage;
    }

    private final TargetRepository targetRepository;

    public SendMessage show_profile(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        Optional<Target> byUser_id = targetRepository.findByUser_IdAndIsDoneFalse(current_user.getId());
        String target_text = null;
        Timestamp time = null;
        String profile = "";

        if (byUser_id.isPresent()) {
            Target target = byUser_id.get();
            target_text = target.getText();
            time = target.getCreatedAt();


        }

        // bitta ish qilish kerak optimal qilish kerak

        profile += ("To'liq ism : " + current_user.getFullName());
        profile += ("\nTelefon raqam : " + current_user.getPhoneNumber());
        profile += ("\nLavozimi : " + current_user.getPosition());
        profile += ("\nEmail  : " + current_user.getEmail());
        profile += ("\nHaftalik reja  : " + (target_text != null ? target_text : "Hali o'rnatilmagan"));
        if (time != null) {
            profile += ("\nVaqti  : " + time.toString().substring(0, 10) + " " + time.toString().substring(11, 19));
        }

        // buttons
        sendMessage.setText(profile);

        return sendMessage;
    }

    public SendMessage setting_weekly_target(Update update, BotUser current_user) {

        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        // agar target avval o'rnatgan bo'lsa :
        Optional<Target> byUser_id = targetRepository.findByUser_IdAndIsDoneFalse(current_user.getId());
        if (byUser_id.isPresent()) {
            Target target = byUser_id.get();
            // chiroyli qip chiqarsa bo'ladi inshallah
            sendMessage.setText("Siz allaqchon reja o'rnatib bo'lgansiz :\n" +
                    " " + target.getText());
            return sendMessage;
        }

        current_user.setState(State.SETTING_TARGET);
        userRepository.save(current_user);

        sendMessage.setText("Haftalik rejangizni kiriting : ");

        // agar button qo'shib yubormasam avvalgi buttonnlar ko'rinib turadi
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();

//         button:
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.BACK); // ortga tugmasi
        row.add(button);
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;

    }

    public SendMessage setting_target(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        // target keladi nasib bo'lsa
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        // buttons


        // ortga deb bosganda
        if (text.equals(Buttons.BACK)) {
            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }
            return sendMessage;
        }

        // target yasab qo'yish kerak
        Target target = new Target();
        target.setText(text);
        target.setUser(current_user);
        targetRepository.save(target);

        sendMessage.setText("Shu rejani o'rnatishga rozimisz ?\n\n" +
                "Eslatma buni qaytib o'zgartira olmaysiz \n\n" +
                "Rejangiz : " + text);

        current_user.setState(State.SUBMIT_TARGET);
        userRepository.save(current_user);
        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
        KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


        row.add(button);
        row.add(button1);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;

    }

    public SendMessage submit_target(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        //button keladi :
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (text.equals(Buttons.ACCEPT)) {
            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }

            sendMessage.setText("Rejangiz muvaffaqiyatli o'rnatildi!");
            return sendMessage;
        } else if (text.equals(Buttons.REJECT)) {

            Optional<Target> byUser_id = targetRepository.findByUser_IdAndIsDoneFalse(current_user.getId());
            if (byUser_id.isPresent()) {
                Target target = byUser_id.get();
                targetRepository.delete(target);
                // target o'chirildi
            }

            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }

            return sendMessage;
        } else {
            sendMessage.setText("noto'g'ri ma'lumot kiritildi,\n" +
                    "Iltimos Tugmalardan birini bosing!");

        }


        // buttons


        return sendMessage;
    }

    public SendMessage setting_result(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        // buttons
        current_user.setState(State.SETTING_RESULT);
        userRepository.save(current_user);

        sendMessage.setText("Ushbu haftadagi rejangiz natijasini kiriting ");

        // agar button qo'shib yubormasam avvalgi buttonnlar ko'rinib turadi
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();

//         button:
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.BACK);
        row.add(button);
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage settingResult(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));


        if (text.equals(Buttons.BACK)) {
            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }
            return sendMessage;
        }


        // result keldi:
        Optional<Target> byUser_id = targetRepository.findByUser_IdAndIsDoneFalse(current_user.getId());
        Target target = byUser_id.get();

        target.setResults(text);
        target.setFinishedAt(new Timestamp(new Date().getTime()));
        target.setDone(true);
        targetRepository.save(target);

        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
        KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


        row.add(button);
        row.add(button1);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);


        sendMessage.setText("Ushbu haftaga bo'lgan rejaga xulosa ni tasdiqlaysizmi ? :\n" +
                "" + text);

        current_user.setState(State.SUBMIT_RESULT);
        userRepository.save(current_user);

        return sendMessage;
    }

    public SendMessage submit_result(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        //button keladi :
        String text = message.getText();
        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (text.equals(Buttons.ACCEPT)) {
            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }

            sendMessage.setText("Xulosa muvaffaqiyatli o'rnatildi!");
            return sendMessage;
        } else if (text.equals(Buttons.REJECT)) {

            Optional<Target> byUser_id = targetRepository.findByUser_IdAndIsDoneFalse(current_user.getId());
            if (byUser_id.isPresent()) {
                Target target = byUser_id.get();
                target.setResults(null);
                target.setFinishedAt(null);
                target.setDone(false);
                targetRepository.save(target);
            }

            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }

            return sendMessage;
        } else {
            sendMessage.setText("noto'g'ri ma'lumot kiritildi,\n" +
                    "Iltimos Tugmalardan birini bosing!");

        }

        return sendMessage;
    }

    public SendMessage inbox(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        // hammaning xabarlarini ko'rsatishimiza kerak !
        List<Messages> messagesList = messagesRepository.findAllByIsSentTrueAndType(MessageType.COMMENT);

        // kimdanligi , qachon , text

        String xabar = "";

        for (Messages messagecha : messagesList) {
            Timestamp time = messagecha.getCreatedAt();
            xabar += ("\nXodim : " + messagecha.getSender().getFullName() + "\nVaqti : " + time.toString().substring(0, 10) + " " + time.toString().substring(11, 19) + "\nXabar matni : " + messagecha.getText());

            xabar += "\n=============\n\n";
        }

        if (xabar.equals("")){
            sendMessage.setText("Hali xabarlar mavjud emas" +
                    "");
        } else {
        sendMessage.setText(xabar);
        }


        return sendMessage;
    }

    public SendMessage allTargets(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        // filterdan o'tqazish kerak 14 kun avvaldan keyingilari kerak


        String xabar = "";
        Timestamp before = new Timestamp(new Date().getTime() - 14 * 24 * 3600 * 1000);


        List<Target> targetList = targetRepository.findAllByCreatedAtAfter(before);


        for (Target target : targetList) {
            xabar += ("\nXodim : " + target.getUser().getFullName());
            xabar += ("\nReja : " + target.getText());
            String time = target.getCreatedAt().toString();
            xabar += ("\nBoshlangan vaqti : " + time.substring(0, 10) + " " + time.substring(11, 19));
            if (target.isDone()) {
                String finished = target.getFinishedAt().toString();
                xabar += ("\nDeadline : " + finished.substring(0, 10) + " " + finished.substring(11, 19));
                xabar += ("\nXulosa : " + target.getResults());
            } else {
                xabar += ("\nXulosa : Hali natija qilinmagan");
            }

            xabar += "\n------------------\n\n";
        }

        if (xabar.equals("")){
            sendMessage.setText("Hali rejalar mavjud emas mavjud emas" +
                    "");
        } else {
            sendMessage.setText(xabar);
        }

//        sendMessage.setText(xabar); // hammasini xabar ko'rinishida chiqaradi nasib bo'lsa
        return sendMessage;
    }

    public SendMessage current_Targets(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));


//        hali yakunlanmagan targetlar

        StringBuilder sb = new StringBuilder("\tHali yakunlanmagan targetlar");

        for (Target target : targetRepository.findAllByIsDoneFalse()) {
            sb.append("\nXodim : " + target.getUser().getFullName());
            sb.append("\nTelefon raqami : " + target.getUser().getPhoneNumber());
            String time = target.getCreatedAt().toString();
            sb.append("\nBoshlangan vaqti : " + time.substring(0, 10) + " " + time.substring(11, 19));
            sb.append("\nLavozimi : " + target.getUser().getPosition());
            sb.append("\nReja matni : " + target.getText());
            sb.append("\n==============\n\n");
        }

        sendMessage.setText(String.valueOf(sb));

        return sendMessage;

    }

    public SendMessage news_button(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        StringBuilder stringBuilder = new StringBuilder("Hamma xodimlar bilishi kerak bo'lgan " +
                "xabar matnini kiriting :  ⤵️");

        sendMessage.setText(stringBuilder.toString());

        current_user.setState(State.SETTING_NEWS);
        userRepository.save(current_user);

        // agar button qo'shib yubormasam avvalgi buttonnlar ko'rinib turadi
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();

//         button:
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.BACK); // ortga tugmasi
        row.add(button);
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage settingNews(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();

        String text = message.getText(); /// button yoki xabar

        Long chatId = message.getChatId();
        sendMessage.setChatId(String.valueOf(chatId));

        if (text.equals(Buttons.BACK)) {
            sendMessage = admin_menu(update, current_user);
            sendMessage.setText("Menudan birontasini tanlashingiz mumkin .");
            return sendMessage;
        }

        // bunda yangilik keladi nasib bo'lsa uni bazaga saqlemiza :
        Messages messages = new Messages();
        messages.setSender(current_user);
        messages.setText(text);
        messages.setType(MessageType.PUBLIC);
        messagesRepository.save(messages); /// keyin isSent bo'yicha topamiza nasib bo'lsa

        // submit buttons :
        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
        KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


        row.add(button);
        row.add(button1);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);


        sendMessage.setText("Ushbu xabarni hammaga yuborish ni tasdiqlaysizmi ? \n" +
                "Xabar : " + text);

        current_user.setState(State.SUBMIT_NEWS);
        userRepository.save(current_user);
        return sendMessage;
    }

    public SendMessage show_all_people(Update update, BotUser current_user) {

        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("Quyidagi userlardan birontasini tanlang");

        List<BotUser> all = userRepository.findAll();


        // o'zidan boshqa hammani chiqarishi kerak nasib bo'lsa :
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (BotUser user : all) {
            List<InlineKeyboardButton> list = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(user.getFullName() + " | " + user.getPhoneNumber());
            button.setCallbackData(user.getChatId()); // id si nasib bo'lsa callback data bo'p boradi :
            list.add(button);
            rowList.add(list);
        }

        current_user.setState(State.CHOOSE_ONE);
        userRepository.save(current_user);

        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

//    public static void main(String[] args) {
//        List<String> list = List.of("asda","asdsad","12");
//        System.out.println("list.size() = " + list.size());
//        list.indexOf(obj)
//        System.out.println("list.size() = " + list.size());
//
//
//    }

    public SendMessage send_private_message(Update update, BotUser current_user) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());
        String data = update.getCallbackQuery().getData();
        Optional<BotUser> byChatId = userRepository.findByChatId(data);
        BotUser user = byChatId.get();
        sendMessage.setText(user.getFullName()+"ga yubormoqchi bo'lgan xabaringizni yozing");


        Messages messages = new Messages();
        messages.setSender(current_user);
        messages.setType(MessageType.PRIVATE);
        messages.setReceiver(user);
        messagesRepository.save(messages);

        current_user.setState(State.SEND_PRIVATE_MESSAGE);
        userRepository.save(current_user);

        // agar keyboardsiz borsa nasib bo'lsa ishlasa bo'ladi

        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.BACK);


        row.add(button);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);



        return sendMessage;
    }

    public SendMessage submit_private_message(Update update, BotUser current_user) {

        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());

        //private message xabari :
        String text = update.getMessage().getText();

        if (text.equals(Buttons.BACK)){
            if (current_user.getRole().equals("admin")) {
                sendMessage = admin_menu(update, current_user);
            } else {
                sendMessage = user_menu(update, current_user);
            }
            sendMessage.setText("Menudan tanlang");
            return sendMessage;
        }


        Messages messages = messagesRepository.findBySender_IdAndIsSentIsFalse(current_user.getId()).get();
        messages.setText(text);
        messagesRepository.save(messages);

        sendMessage.setText("Ushbu xabarni tasdiqlaysizmi  ? \n" +
                "Xabar : "+ text);


        // buttons
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(Buttons.ACCEPT);
        KeyboardButton button1 = new KeyboardButton(Buttons.REJECT);


        row.add(button);
        row.add(button1);

        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);


        current_user.setState(State.SUBMIT_PRIVATE_MESSAGE);
        userRepository.save(current_user);

        return sendMessage;

    }
}
