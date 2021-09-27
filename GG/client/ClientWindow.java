package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Обратите внимание на то, какие задачи сейчас решает класс ClientWindow. В нём сосредаточена почти вся логика
// клиента целиком:
//   1. Визуальный интерфейс (создание окна, панелей, реагирование на события...)
//   2. Сетевой I/O (создание socket и взаимодействие с сервером)
//   3. Настройки клиента и окна (host, port, позиция...)
//
// В таком коде сложнее разбираться, поскольку он объединяет в себе много ответственностей. Его также сложнее
// расширять и тестировать. Но его можно значительно улучшить, если правильно разбить эти задачи на отдельные
// компоненты.
//
// Рекомендую ознакомиться с некоторыми архитектурными подходами к разработке (например, MVC). Попробуйте подумать над
// тем, как можно разбить этот класс на отдельные состовляющие. Например,
//   - ClientView может хранить GUI поля и предоставлять методы взаимодействия с ними
//   - ClientSocket может абстрагировать соединение и I/O операции
//   - ClientController может быть ответственен за работу программы в целом, делегируя подзадачи вышеуказанным
//   компонентам
//
// 🎓 Reference: https://www.educative.io/blog/mvc-tutorial

class ClientWindow extends JFrame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3443;

    private final JTextField jtfMessage;
    private final JTextField jtfName;
    private final JTextArea jtaTextAreaMessage;

    private Socket clientSocket;
    private Scanner inMessage;
    private PrintWriter outMessage;
    private String clientName = "";

    public ClientWindow() {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // Поскольку каждое окно начинается из одной и той же точки, при запуске приложения не сразу ясно, что окна
        // два, ведь одно из них полностью закрывает другое. Предпочтительным было бы принимать (x,y) координаты извне,
        // чтобы метод `main` мог расставить окна рядом друг с другом.
        setBounds(600, 600, 600, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Название тоже можно подобрать уникальное. Скажем, каждому клиенту (окну) можно выдать идентификатор, или
        // просто рабочее название, опять-таки принимая его из метода `main`. Отмечу, что если разделить код на
        // несколько классов, то станет гораздо проще передавать им различные параметры в конструкторах.
        setTitle("Client");

        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);

        var jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);

        var jlNumberOfClients = new JLabel("Current clients in chat:");
        add(jlNumberOfClients, BorderLayout.NORTH);

        var bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        var jbSendMessage = new JButton("Send");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);

        // С точки зрения UI панели ввода имени и сообщения стоит слегка переделать.
        // Двоеточие обычно ставят, если название поля стоит как отдельный Label перед панелью ввода:
        //
        //                     +--------------------+
        // Enter your message: |                    |
        //                     +--------------------+
        //
        // Если же текст находится внутри, то это называется подсказкой, и в таком случае двоеточие не ставится:
        //
        // +--------------------+
        // | Enter your message |
        // +--------------------+
        //
        // Особо интересно в такие моменты посмотреть, как это делают другие приложения. Скажем, Telegram :)
        //
        // Рекомендую также поизучать, как можно реализовать кастомный JTextField с подсказкой. Пример:
        // http://javaswingcomponents.blogspot.com/2012/05/how-to-create-simple-hinttextfield-in.html

        jtfMessage = new JTextField("Enter your message: ");
        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);

        jtfName = new JTextField("Enter your name: ");
        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfName.setText("");
            }
        });
        bottomPanel.add(jtfName, BorderLayout.WEST);

        // Анонимные интерфейсы можно заменять лямбдами.
        jbSendMessage.addActionListener(event -> {
            if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                clientName = jtfName.getText();
                sendMessage();
                jtfMessage.grabFocus();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);
                // Помните: для строк используется `equals`, а не `==`.
                if (!clientName.isEmpty() && !clientName.equals("Enter your name: ")) {
                    outMessage.println(clientName + " left the chat room");
                } else {
                    outMessage.println("The participant left the chat room without introducing himself");
                }
                outMessage.println("##session##end##");
                outMessage.flush();

                outMessage.close();
                inMessage.close();

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Could not close socket: " + e.getMessage());
                }
            }
        });

        setVisible(true);

        // Эту часть конструктора можно выделить в отдельный метод `start()`. Этот код ответственен за то, чтобы
        // "запустить" работу окна, конструкторы же обычно подготавливают код к исполнению. Обратите внимание, что даже
        // класс Thread так устроен. Возможно, другие части конструктора также стоило бы переместить в такой метод.
        //
        // Использование этого класса тогда выглядело бы так:
        // ```
        //   var clientWindow1 = new ClientWindow();
        //   var clientWindow2 = new ClientWindow();
        //
        //   clientWindow1.start();
        //   clientWindow2.start();
        // ```
        var thread = new Thread(() -> {
            while (true) {
                if (inMessage.hasNext()) {
                    String inMsg = inMessage.nextLine();
                    String clientsCount = "Clients count = ";
                    if (inMsg.indexOf(clientsCount) == 0) {
                        jlNumberOfClients.setText(inMsg);
                    } else {
                        jtaTextAreaMessage.append(inMsg);
                        jtaTextAreaMessage.append("\n");
                    }
                }
            }
        });
        thread.start();
    }

    // В Java обычно принято не сокращать слова если ono не является какой-то общепринятой конвенцией.
    // Название `sendMessage`, хотя и более вербозно, читается красивее :)
    public void sendMessage() {
        String message = jtfName.getText() + ": " + jtfMessage.getText();
        outMessage.println(message);
        outMessage.flush();
        jtfMessage.setText("");
    }
}
