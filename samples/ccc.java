class ccc {
    public static void main(String[] args) {
        int[] a;
        int b;
        int c;
        int ccc;
        int d;
        int ddd;
        A t;
        c = 1;
        ccc = 0;
        d = 100;
        a = new int[101];
        ddd = ccc - c;
        b = a[ddd];

		System.out.println(a.length);
		System.out.println(a[55]);
        System.out.println(new A1().a());
        System.out.println(new A2().b());
        System.out.println(new A3().a());
        System.out.println(new A3().b());

        t = new C();
        System.out.println(t.i());
    }
}

class C extends A {
    public int j() {
        return 666;
    }

    public int i() {
        int[] p;
        C c;
        c = new C();
        i = 99999999;

        p = new int[10];

        if (i < 10)
            p = new int[22];
        else
            p = new int[30];
        i = p[25];
        return c.index(c.j(), false && (c.kk()), p, new B());
    }

    public boolean kk() {
        return true;
    }
}

class A {
    B b;
    int i;

    public int index(int uu, boolean jj, int[] qq, B bb) {
        return uu;
    }

    public int i() {
        int i;
        A a;

        a = new C();
        i = a.j();
        return 0;
    }

    public int j() {
        int[] b;
        int a;
        b = new int[100];
        a = b[0];

        return a;
    }

    public B k() {
        return new B();
    }
}

class B {
}

class A1 {
    int i;
    
    public int a() {
        return 1;
    }
}

class A2 extends A1 {
    boolean i;

    public int b() {
        return 2;
    }
}

class A3 extends A2 {
    boolean i;

    public int a() {
        return 3;
    }

    public int b() {
        return 4;
    }
}