package org.springframework.hateoas.hal;

public class SimplePojo {

    private String text;
    private int number;

    public SimplePojo() {
    }

    public SimplePojo(String text, int number) {
        this.text = text;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
