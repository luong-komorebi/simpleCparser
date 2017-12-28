

class Errors {
    static void fatal(int lineNum, int charNum, String msg) {
        System.err.println("At line: " + lineNum + ", column: " + charNum
                + " **ERROR** " + msg);
        fatalError = true;
    }

    static void warn(int lineNum, int charNum, String msg) {
        System.err.println("At line: " + lineNum + ", column: " + charNum
                + " **WARNING** " + msg);
    }

    static boolean fatalError = false;
}
