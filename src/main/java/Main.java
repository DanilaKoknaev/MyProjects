public class Main {
    public static void main(String[] args) {
        MyStringBuilder msb = new MyStringBuilder();
        msb.append("Hello").append(", my friend");
        System.out.println(msb.toString());
        msb.undo();
        System.out.println(msb.toString());
        msb.undo();
        msb.undo();
        msb.undo();
        System.out.println(msb.toString());
        msb.append("Hello");
        System.out.println(msb.toString());
    }
}

//Изучите внутреннюю реализацию класса StringBuilder и напишите свою с добавлением дополнительного метода - undo().
//
//Прежде чем приступать - прочитайте про паттерн snapshot и примените его в своей реализации, понимайте
// паттерн не буквально, помните что его можно реализовать по разному сохраняя его смысл.
//
//примечание: полностью переписывать все методы которые есть в StringBuilder не нужно,
// в задании важно именно понимание сути паттерна. В случае, если задание остаётся непонятным, задайте вопрос ментору.