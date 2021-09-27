package client;

// Основной класс необязательно называть Main. Лучше всего каждой программе давать уникальное название, особенно
// если программ в пределах одного проекта несколько (как и здесь). Тогда их легче разчилать как для вас, так и для IDE.
public class ClientApplication {

    public static void main(String[] args) {
        // Чисто придирка, но я предпочитаю ради симметрии ставить числа в конце каждой переменной.
        // Также, если использовать достаточно новую версию языка, предпочитайте использовать var
        // при объявлении переменной, если её тип очевиден.
        // 🎓 Reference: https://www.baeldung.com/java-10-local-variable-type-inference
         var clientWindow1 = new ClientWindow();
         var clientWindow2 = new ClientWindow();
    }
}