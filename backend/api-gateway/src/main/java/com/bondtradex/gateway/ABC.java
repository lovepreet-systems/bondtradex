package com.bondtradex.gateway;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ABC {

    public static void main(String args[]){
        BCD b1=new BCD("zabc","efg");
        BCD b2=new BCD("yabc","efg");
        BCD b3=new BCD("xabc","bcd");
        BCD b4=new BCD("wabc","abc");
        List<BCD> people = new ArrayList<>();
        people.add(b1);
        people.add(b2);
        people.add(b3);
        people.add(b4);
        System.out.println("Before Sort : "  + people);
        Comparator<BCD> comparator=new Comparator<BCD>() {
            @Override
            public int compare(BCD b1,BCD b2){
                return b1.firstName.compareTo(b2.firstName);
            }
        };
        Comparator.comparing(BCD::getFirstName).thenComparing(BCD::getLastName);
        Collections.sort(people,comparator);
        System.out.println("After Sort : "  + people);
    }
    static class BCD{
        String firstName;
        String lastName;

        public BCD(String firstName,String lastName){
            this.firstName=firstName;
            this.lastName=lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        @Override
        public String toString() {
            return "BCD{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }

    }
}
