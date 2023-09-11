package com.styzf.link.parser.dto.method;

import cn.hutool.core.collection.CollUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * 调用链路解析，支持双向树结构
 *
 * @author styzf
 * @date 2023/9/7 22:31
 */
public class MethodCallTree {
    /**
     * 根方法名
     */
    private String rootMethodName;
    
    /**
     * 下一链路
     */
    private List<MethodCallTree> nextList = new ArrayList<>();
    
    /**
     * 上一链路
     */
    private List<MethodCallTree> prevList = new ArrayList<>();
    
    /**
     * 根数据
     */
    private MethodCallTree root;
    
    /**
     * 根方法调用的方法
     */
    private List<MethodCall> callList = new ArrayList<>();
    
    /**
     * 被调用方
     */
    private List<MethodCall> calleeList = new ArrayList<>();
    
    /**
     * 树级别，默认为0
     */
    private int level = 0;
    
    /**
     * 是否为上级方法判断调用
     */
    private boolean ifMethodBlock = false;
    
    /**
     * 是否为上级方法for循环调用
     */
    private boolean forMethodBlock = false;
    
    /**
     * 用于去重的set
     */
    public static final Set<String> METHOD_SET = new HashSet<>();
    
    /**
     * 初始化方法
     * @param rootMethodName 要解析的方法名
     */
    public static MethodCallTree init(String rootMethodName) {
        MethodCallTree root = new MethodCallTree();
        root.rootMethodName = rootMethodName;
        root.root = root;
        
        return root;
    }
    
    /**
     * 向下解析
     */
    public void nextParser() {
        this.callList = DataContext.METHOD_CALL_MAP.get(this.rootMethodName);
        if (CollUtil.isEmpty(this.callList)) {
            MethodCallTree.METHOD_SET.remove(this.rootMethodName);
            return;
        }
        if (!MethodCallTree.METHOD_SET.add(this.rootMethodName)) {
            return;
        }
        
        for (MethodCall next : this.callList) {
            MethodCallTree nextMethodCallTree = MethodCallTree.init(next.genCalleeFullMethod());
            nextMethodCallTree.prevList.add(this);
            nextMethodCallTree.calleeList.add(next);
            nextMethodCallTree.level = this.level + 1;
            this.nextList.add(nextMethodCallTree);

            try {
                nextMethodCallTree.nextParser();
            } catch (Throwable e) {
                System.out.println(nextMethodCallTree);
            }
        }
        MethodCallTree.METHOD_SET.remove(this.rootMethodName);
    }
    
    public String getRootMethodName() {
        return rootMethodName;
    }
    
    public String getEasyMethodName() {
        Pattern pattern = compile("(?<=:).*?(?=\\()");
        Matcher matcher = pattern.matcher(this.rootMethodName);
        String methodName = "";
        if (matcher.find()) {
            methodName = matcher.group();
        }

        pattern = compile("(?<=\\.)((.(?!(\\.|\\()))*.)(?=(,|\\)))");
        matcher = pattern.matcher(this.rootMethodName);
        StringBuilder args = new StringBuilder();
        while (matcher.find()) {
            args.append("_").append(matcher.group());
        }
        
        return getEasyClassName() + "_" + methodName + args;
    }
    
    public String getEasyClassName() {
        Pattern pattern = compile("(?<=\\.)((.(?!\\.))*.)(?=:)");
        Matcher matcher = pattern.matcher(this.rootMethodName);
        String className = "";
        if (matcher.find()) {
            className = matcher.group();
        }
    
        return className;
    }
    
    public String getClassName() {
        Pattern pattern = compile("(?<=\\.)((.(?!\\.))*.)(?=:)");
        Matcher matcher = pattern.matcher(this.rootMethodName);
        String className = "";
        if (matcher.find()) {
            className = matcher.group();
        }
        
        return this.rootMethodName.substring(0, this.rootMethodName.indexOf(":"));
    }
    
    public List<MethodCallTree> getNextList() {
        return nextList;
    }
    
    public List<MethodCallTree> getPrevList() {
        return prevList;
    }
    
    public MethodCallTree getRoot() {
        return root;
    }
    
    public List<MethodCall> getCallList() {
        return callList;
    }
    
    public List<MethodCall> getCalleeList() {
        return calleeList;
    }
    
    public int getLevel() {
        return level;
    }
}
