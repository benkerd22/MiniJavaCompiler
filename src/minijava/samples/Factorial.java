class Factorial{
    public static void main(String[] a){
        System.out.println(new Fac().ComputeFac(10));
    }
}

class Fac {
    public int ComputeFac(int num){
        int num_aux ;
        if (num < 1)
            num_aux = 1 ;
        else
            num_aux = num * (this.ComputeFac(num-1)) ;
        return num_aux ;
    }
}

class C extends A {
    public int i() {
        i = 1;

        return i;
    }
}

class A {
    B b;
    int i;

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

