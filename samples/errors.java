class errors {
    public static void main(String[] args) {
    }
}

class C1 {
    public int f1() {
        CCC a;
        return 0;
    }

    public int f2() {
        b = 1;
        return 0;
    }

    public int f3() {
        C1 a;
        a = new C1();

        return a.u1();
    }
}

class C1 {}

class C2 {
    int i;
    int i;
    public int f1() {
        return 0;
    }
    public int f1() {
        return 0;
    }
    public int f2(int a, boolean b) {
        boolean a;
        return 0;
    }
    public int f3() {
        int a;
        int a;
        return 0;
    }
}

class C3 {
    public int f1() {
        int[] a;
        a = new int[10];
        if (1) {} else {}
        if (a) {} else {}
        if (a[1]) {} else {}
        while (1) {}
        while (a) {}
        while (a[1]) {}

        return 0;
    }

    public int f2() {
        int[] a;
        boolean b;
        a = new int[10];
        b = true;

        System.out.println(b);
        System.out.println(a);
        System.out.println(true);

        return 0;
    }

    public int f3() {
        int a;
        return 0;
    }
}