import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 18:32
 * Description:
 */
public class MainTest {

    public static void main(String[] args){
        try {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext.xml"});
            context.start();
            System.in.read();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
