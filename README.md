# 1、说明

项目基于[java-callgraph2](https://github.com/Adrninistrator/java-callgraph2)开发，主要目的在于Java调用链路的解析。具体项目内容可以看源项目链接，目前的改动并不大。

# 2、现状变更说明

废弃原有开始类，重新写了一个`com.styzf.link.parser.stat.CallDataGenerate`，项目入口在这里调用main方法即可生成数据

对比原有的类，有以下几点变更

1、把io流输出的代码删了，这一个可以参考原先的代码，准备把这块写到生成器中去

2、基于现状，写了一个`com.styzf.link.parser.parser.ParserInterface`，并通过抽象`com.styzf.link.parser.parser.AbstractLinkParser`完成数据链路解析，其他生成器通过内嵌解析器进行数据的解析和生成

3、定义了文件生成类`com.styzf.link.parser.generator.FileGenerate`，在txt包中实现原先所有的输出功能，另外添加解析到接口，以及解析到数据库的功能实现

4、xmind包，可以生成xmind文件，puml包，用于生成puml文件

5、框架底层用的[bcel](https://commons.apache.org/proper/commons-bcel/manual/bcel-api.html)，作者已经做了基础的解析工作了，用的时候可以学习了解一下

# 3、目前待完成需求内容

1、将原先项目io生成的文件放到`com.styzf.link.parser.generator.txt`包中实现，并且按照新的规则实现接口方法`com.styzf.link.parser.generator.FileGenerate`（目前只实现了一个调用关系的文件生成处理）

2、txt生成的文件添加一个向上调用解析末级方法的功能，一个向下解析末级方法的功能。需要有一个截止判断

3、上下文解析适配spring接口、mybatis，做好能获取到接口信息、mybatis的sql解析，spring类的注解原作者好像有做解析，可以做一下参考，mybatis应该要写扩展解析功能

4、实现从接口解析，解析出来所有可能会调用到的数据库sql

5、从mybatis的类或者方法解析，解析出来所有可能调用到的接口，并展示所有的调用链路

6、解析是否为if条件括号中调用的方法，判断是否在if大括号中的方法，判断为true调用还是false调用

7、解析是否为for循环内调用

8、文档注释解析（已完成）部分功能不完善，例如继承的情况下，框架解析的是接口入参类型，而文档解析出来的是实现类的类型

9、数据量过大的时候，考虑分多个文件输出，或者存储在数据库中，具体怎么拆分具体再考虑

10、解析和调用的入参都取消，全部通过上下文进行传参，上下文应当考虑扩展问题

11、接口调用实现，涉及多类实现的，尽量确认具体的调用方（重点，优先处理）

例如：a接口，有b、c、d三个实现，实现类各自实现了a接口方法，b调用链路应该是“调用方”->a->b，而不是“调用方”->a->b、c、d

# 4、已完成需求

1、解析调用链路`com.styzf.link.parser.parser.AbstractLinkParser`，辨别是否为循环调用`com.styzf.link.parser.parser.AbstractLinkParser.loopHandle`

2、调用关系文件生成`com.styzf.link.parser.generator.txt.MethodCallTxtGeneratot`

3、解析过程添加正则筛选

4、文件生成添加计时器

5、添加pom文件加载功能，通过解析pom文件中所有jar，完成解析

6、接口有默认实现的方法，子类调用接口方法直到出现子类有实现方法为止
