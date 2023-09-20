package test.maven;

import com.styzf.link.parser.util.MavenUtils;

import java.io.File;
import java.util.List;

/**
 * @author styzf
 * @date 2023/9/20 22:23
 */
public class MavenTest {
    
    public static void main(String[] args) {
        File pomFile = new File("D:\\dev\\myWorkSpace\\java-callgraph2-main\\pom.xml");
        List<String> depList = MavenUtils.getDepList(pomFile);
        depList.forEach(System.out::println);
    }
}
