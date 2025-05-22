package jp.enbind.spring_ai.part1.command;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class SampleCommand {

    @ShellMethod(key = "hello")
    String hello(){
        return "hello world";
    }
}
