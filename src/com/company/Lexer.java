package com.company;

import java.util.Vector;
import java.io.*;

public class Lexer {
    public int syn,index;
    public String input_code;   // 输入代码
    public Vector<Token> tokens;    // 生成的token序列
    public String[] keyword={"if","then","else","while","do"},
                    op={"\'", "+","-","*","/", "!=", ">",">=", "<","<=", "=","==", "(",")",";",};
    public Vector<String> idTable,numTable;


    public static boolean isLetter(char letter){
        if(letter>='a'&&letter<='z'||letter>='A'&&letter<='Z'||letter=='_')
            return true;
        else
            return false;
    }

    public static boolean isDigit(char digit)
    {
        if(digit>='0'&&digit<='9')
            return true;
        else
            return false;
    }

    public void readfile(){
        // 读源程序文件
        try{
            char temp_char;
            File file = new File("E://input.txt");
            Reader reader = new InputStreamReader(new FileInputStream(file));
            while((temp_char=(char) reader.read())!='$'){
                input_code=input_code+temp_char;
            }
            reader.close();
        }catch (IOException e){
            System.out.println("IOException.");
        }
    }

    public void pre_op(){
        // 预处理，过滤空格和注释
        StringBuffer tempString = new StringBuffer();
        int count=0;

        //对注释的处理
        for(int i=0; i<input_code.length(); i++)
        {// 扫描每一个字符

            if(input_code.charAt(i)=='/'&&input_code.charAt(i+1)=='/')
            /**扫描到'//'，则略过这行**/
                while(input_code.charAt(i)!='\n')
                    i++;
            if(input_code.charAt(i)=='/'&&input_code.charAt(i+1)=='*')
            {   // 扫描'/** **/'类型的注释
                i+=2;
                while(input_code.charAt(i)!='*'||input_code.charAt(i+1)!='/')
                {
                    i++;
                    if(input_code.charAt(i)=='$')
                    {
                        System.out.println("注释出错");
                        return;
                    }
                }
                i+=2;
            }
            if(input_code.charAt(i)!='\n'&&input_code.charAt(i)!='\t'&&input_code.charAt(i)!='\r')
            {   // 遇到不是以下情况：换行/制表符/垂直制表/回车，count++
                tempString.insert(count++,input_code.charAt(i));
            }
        }
        tempString.insert(count,'\0');
        input_code=tempString.toString();
    }

    public int searchTable(String table[],String str){
        for (int i=0;i<table.length;i++){
            if(table[i].equals(str)){
                return i+1;
                // 返回i+1因为，0代表$
            }
        }
        return -1; // 没找到返回-1
    }

    public int searchTable(Vector<String> table,String str){
        for (int i=0;i<table.size();i++){
            if(table.get(i).equals(str)){
                return i+1;
                // 返回i+1因为，0代表$
            }
        }
        return -1; // 没找到返回-1
    }

    public Token Scanner()
    {
        Token token = new Token();
        StringBuffer morpheme = new StringBuffer();

        while(input_code.charAt(index)==' '){
            // 跳过空白字符
            index++;
        }
        char ch = input_code.charAt(index);

        if (isLetter(ch))
        {
            morpheme.append(ch);
            index++;
            ch = input_code.charAt(index);

            while(isLetter(ch)||isDigit(ch))
            {   // 继续读入字符和数字
                morpheme.append(ch);
                index++;
                ch = input_code.charAt(index);
            }

            syn = searchTable(keyword,morpheme.toString());

            if (syn!=-1){
                // is a keyword
                token.type=keyword[syn-1];
                token.value=null;
                tokens.add(token);
            }
            else{
                // not a keyword
                syn=searchTable(idTable,morpheme.toString());

                if (syn==-1){
                    // 不是已有的标识符
                    idTable.add(morpheme.toString());
                    syn = idTable.size();
                }

                token.type="id";
                token.value=Integer.toString(syn-1);
                tokens.add(token);
            }
        }
        else if (isDigit(ch))
        {
            // 若第一个是数字
            while(isDigit(ch))
            {   // 若继续是数字
                morpheme.append(ch);
                index++;
                ch = input_code.charAt(index);
            }

            syn=searchTable(numTable,morpheme.toString());

            if(syn==-1)
            {
                numTable.add(morpheme.toString());
                syn=numTable.size();
            }

            token.type="num";
            token.value=Integer.toString(syn-1);
            tokens.add(token);
        }
        else if (   ch == '+' || ch == '-' || ch == '*' || ch == '/' ||
                    ch == ';' || ch == '(' || ch == ')' || ch == '\'')
        {
            //若为运算符或者界符，查表得到结果
            morpheme.append(ch);

            for (int i = 0; i<op.length; i++)
            {
                //查运算符界符表
                if (op[i].equals(morpheme))
                {
                    syn = i;
                    break;
                }
            }

            token.type="op";
            token.value=Integer.toString(syn);
            tokens.add(token);

            index++;//指针下移，为下一扫描做准备

        }
        else if(ch=='<')
        {
            //<,<=
            index++;
            ch = input_code.charAt(index);

            if(ch=='='){
                syn=searchTable(op,"<=");  // '<='
            }
            else
            {
                index--;
                syn=searchTable(op,"<");  // '<'
            }

            token.type="op";
            token.value=Integer.toString(syn);
            tokens.add(token);

            index++;

        }
        else if(ch=='>')
        {
            //>,>=,>>
            index++;
            ch = input_code.charAt(index);

            if(ch=='='){
                syn=searchTable(op,">=");  // '>='
            }
            else
            {
                index--;
                syn=searchTable(op,">");  // '>'
            }

            token.type="op";
            token.value=Integer.toString(syn);
            tokens.add(token);

            index++;

        }
        else if(ch=='=')
        {
            //=,==
            index++;
            ch=input_code.charAt(index);

            if(ch =='='){
                syn=searchTable(op,"=="); // '=='
            }
            else
            {
                index--;
                syn=searchTable(op,"=");  //  '='
            }

            token.type="op";
            token.value=Integer.toString(syn);
            tokens.add(token);

            index++;

        }
        else if(input_code.charAt(index)=='!' && input_code.charAt(index+1)=='=')
        {
            index+=2;
            syn=searchTable(op,"!=");
            token.type="op";
            token.value=Integer.toString(syn);
            tokens.add(token);
        }
        else if(ch=='\0')
        {
            syn=0;
            token.type="$";
            token.value=null;
            tokens.add(token);
        }
        else
        {

            System.out.println("error: no exist Lexer:"+input_code.charAt(index));
            System.exit(0);
        }
        return token;
    }

    public Lexer(){
        syn=-1;
        index=0;
        input_code = new String();
        tokens = new Vector<Token>();
        idTable = new Vector<String>();
        numTable = new Vector<String>();
        readfile();
        pre_op();
        //System.out.println(input_code);
        while(syn!=0){
            Scanner();
        }
    }
}

class Token {
    String type;
    String value;
}

