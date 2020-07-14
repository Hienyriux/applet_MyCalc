package com.example.mycalc;

import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import java.util.Stack;
import java.util.Vector;
import java.math.BigDecimal;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String str = "";
    private TextView dis_area;

    boolean parenMatch() {
        int top = 0;
        for(int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == '(')
                top++;
            else if(str.charAt(i) == ')')
                top--;
            if(top < 0)
                break;
        }
        return top == 0;
    }
    void minusTrans(){
        char[] _str = str.toCharArray();
        for(int i = 0; i < str.length(); i++) {
            if(_str[i] == '－') {
                if((_str[i + 1] >= '0' &&  _str[i + 1] <= '9') || _str[i + 1] == '.') {
                    if (i == 0)
                        _str[i] = '-';
                    else if (_str[i - 1] == '(')
                        _str[i] = '-';
                }
            }
        }
        str = String.valueOf(_str);
    }
    int isp(char ch) {
        switch (ch) {
            case ')': return 8;
            case '^': case 'L': return 7;
            case '÷': case '×': return 5;
            case '－': case '+': return 3;
            case '(': return 1;
            case ';': return 0;
            default: return -1;
        }
    }

    int icp(char ch) {
        switch (ch) {
            case '(': return 8;
            case '^': case 'L': return 6;
            case '÷': case '×': return 4;
            case '－': case '+': return 2;
            case ')': return 1;
            case ';': return 0;
            default: return -1;
        }
    }

    void calcProcess() {
        boolean isOk = true;
        if(!parenMatch()) {
            str = "括号不匹配";
            isOk = false;
        }
        minusTrans();
        str += ';';
        Stack<Character> oprt = new Stack<Character>();
        oprt.push(';');
        Vector<BigDecimal> oprd = new Vector<>();
        Vector<Boolean> isoprd = new Vector<>();
        Vector<Character> ordered_oprt = new Vector<>();
        int start_pos = -1;
        for(int i = 0; i < str.length(); i++) {
            // 注意，小于1的小数可以直接以小数点开头
            if(start_pos == -1 && ((str.charAt(i) >= '0' && str.charAt(i) <= '9') || str.charAt(i) == '.' || str.charAt(i) == '-'))
                start_pos = i;
            else if(!(str.charAt(i) >= '0' && str.charAt(i) <= '9') && str.charAt(i) != '.'){
                int cur_isp = isp(oprt.peek());
                int cur_icp = icp(str.charAt(i));
                if(cur_icp == -1){
                    str = "未知错误";
                    isOk = false;
                    break;
                }
                if(start_pos != -1) {
                    BigDecimal tmp;
                    try {
                        tmp = new BigDecimal(str.substring(start_pos, i));
                    } catch (NumberFormatException e) {
                        str = "小数格式错误";
                        isOk = false;
                        break;
                    }
                    oprd.add(tmp);
                    isoprd.add(true);
                    start_pos = -1;
                }
                while(true) {
                    if(cur_icp == 0 && cur_isp == 0)
                        break;
                    if (cur_icp > cur_isp) {
                        oprt.push(str.charAt(i));
                        break;
                    } else if (cur_icp < cur_isp) {
                        oprd.add(BigDecimal.ZERO);
                        isoprd.add(false);
                        ordered_oprt.add(oprt.pop());
                        cur_isp = isp(oprt.peek());
                    } else if (oprt.pop() == '(')
                        break;
                }
            }
        }
        if(isOk){
            int oprt_pos = 0;
            Stack<BigDecimal> ordered_oprd = new Stack<BigDecimal>();
            for(int i = 0; i < oprd.size(); i++){
                if(!isoprd.elementAt(i)){
                    if(ordered_oprd.size() < 2){
                        str = "未知错误";
                        isOk = false;
                        break;
                    }
                    BigDecimal right = ordered_oprd.pop();
                    BigDecimal left = ordered_oprd.pop();
                    switch (ordered_oprt.elementAt(oprt_pos)){
                        case '^':{
                            // 整数
                            if(new BigDecimal(right.intValue()).compareTo(right) == 0)
                                ordered_oprd.push(left.pow(right.intValue()));
                            else
                                ordered_oprd.push(BigDecimal.valueOf(Math.pow(left.doubleValue(), right.doubleValue())));
                        } break;
                        case 'L': {
                            if(left.compareTo(BigDecimal.valueOf(0)) <= 0 || left.compareTo(BigDecimal.valueOf(1)) == 0) {
                                str = "底数错误";
                                isOk = false;
                            }
                            else if (right.compareTo(BigDecimal.valueOf(0)) <= 0){
                                str = "真数错误";
                                isOk = false;
                            }
                            else
                                ordered_oprd.push(BigDecimal.valueOf(Math.log(right.doubleValue()) / Math.log(left.doubleValue())));
                            break;
                        }
                        case '÷':{
                            if(right.compareTo(BigDecimal.valueOf(0)) == 0) {
                                str = "除数为零";
                                isOk = false;
                            }
                            else{
                                try {
                                    ordered_oprd.push(left.divide(right));
                                }
                                catch (ArithmeticException e){
                                    ordered_oprd.push(BigDecimal.valueOf(left.doubleValue() / right.doubleValue()));
                                }
                            }

                            break;
                        }
                        case '×': ordered_oprd.push(left.multiply(right)); break;
                        case '－': ordered_oprd.push(left.subtract(right)); break;
                        case '+': ordered_oprd.push(left.add(right)); break;
                        default:{
                            str = "未知错误";
                            isOk = false;
                        }
                    }
                    oprt_pos++;
                    if(!isOk)
                        break;
                }
                else
                    ordered_oprd.push(oprd.elementAt(i));
            }
            if(isOk)
                str = String.valueOf(ordered_oprd.pop());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button backSpace = (Button) findViewById(R.id.backSpace);
        Button clear = (Button) findViewById(R.id.clear);
        Button power = (Button) findViewById(R.id.power);
        Button division = (Button) findViewById(R.id.division);
        Button multi = (Button) findViewById(R.id.multi);
        Button minus = (Button) findViewById(R.id.minus);
        Button plus = (Button) findViewById(R.id.plus);
        Button leftParen = (Button) findViewById(R.id.leftParen);
        Button rightParen = (Button) findViewById(R.id.rightParen);
        Button dot = (Button) findViewById(R.id.dot);
        Button rlog = (Button) findViewById(R.id.rlog);
        Button equal = (Button) findViewById(R.id.equal);
        Button num0 = (Button) findViewById(R.id.num0);
        Button num1 = (Button) findViewById(R.id.num1);
        Button num2 = (Button) findViewById(R.id.num2);
        Button num3 = (Button) findViewById(R.id.num3);
        Button num4 = (Button) findViewById(R.id.num4);
        Button num5 = (Button) findViewById(R.id.num5);
        Button num6 = (Button) findViewById(R.id.num6);
        Button num7 = (Button) findViewById(R.id.num7);
        Button num8 = (Button) findViewById(R.id.num8);
        Button num9 = (Button) findViewById(R.id.num9);
        Button nume = (Button) findViewById(R.id.nume);
        dis_area = (TextView) findViewById(R.id.dis_area);

        backSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(str.length() > 0) {
                    str = str.substring(0, str.length() - 1);
                    dis_area.setText(str);
                    dis_area.invalidate();
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(str.length() > 0) {
                    str = "";
                    dis_area.setText(str);
                    dis_area.invalidate();
                }
            }
        });
        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '^';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        division.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '÷';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        multi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '×';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '－';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '+';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        leftParen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '(';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        rightParen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += ')';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '.';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        rlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += 'L';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcProcess();
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '0';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '1';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '2';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '3';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '4';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '5';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '6';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '7';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '8';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        num9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += '9';
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
        nume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str += "2.71828";
                dis_area.setText(str);
                dis_area.invalidate();
            }
        });
    }
}