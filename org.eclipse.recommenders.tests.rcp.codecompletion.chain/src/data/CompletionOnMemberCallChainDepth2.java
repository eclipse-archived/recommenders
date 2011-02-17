package data;

import java.io.File;

public class CompletionOnMemberCallChainDepth2 {
    class A {
        public B b = new B();

        class B {
            public File findMe = new File("");
        }
    }
    
    A a = new A();
    File c =<^Space>

    public CompletionOnMemberCallChainDepth2(){
        final A a = new A();
        final File c =<^Space>
    }
    
    public void method() {
        final A a = new A();
        final File c =<^Space>
    }
}
