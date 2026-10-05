package com.grabthesevehicles.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/** Run with tools/test_policy.sh; checks interval limits and message rotation. */
public final class PolicyTest {
    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(IntervalPolicy.valid(30,60),"defaults");
        check(IntervalPolicy.valid(15,1440),"supported endpoints");
        check(!IntervalPolicy.valid(14,60),"too frequent");
        check(!IntervalPolicy.valid(30,1441),"too long");
        check(!IntervalPolicy.valid(60,30),"inverted interval");
        check(IntervalPolicy.delayMillis(30,30,new Random(1))==1_800_000L,"fixed interval");
        Random random = new Random(42);
        HashSet<Long> samples = new HashSet<>();
        for(int i=0;i<100_000;i++) {
            long delay=IntervalPolicy.delayMillis(30,60,random);
            check(delay>=1_800_000L && delay<=3_600_000L,"delay outside selected interval");
            samples.add(delay);
        }
        check(samples.size()>90_000,"random delay variety");
        check(VehicleMessages.count()==16,"all 16 lists");
        HashSet<String> messages=new HashSet<>();
        for(int i=0;i<VehicleMessages.count();i++) {
            String message=VehicleMessages.message(i);
            check(message.startsWith("Grab these vehicles: "),"game text prefix");
            check(message.endsWith("."),"game text punctuation");
            check(message.split(", ").length==5,"five vehicles per message");
            check(messages.add(message),"duplicate text");
        }
        for(int previous=-1;previous<16;previous++) {
            for(int cycle=0;cycle<100;cycle++) {
                int[] bag=IntervalPolicy.freshBag(16,previous,random);
                check(bag[0]!=previous,"immediate repeat across bags");
                int[] sorted=bag.clone();Arrays.sort(sorted);
                for(int i=0;i<16;i++)check(sorted[i]==i,"bag must contain every list exactly once");
            }
        }
        System.out.println("Passed: interval bounds, 100,000 random waits, 16 complete vehicle lists, shuffle coverage and no consecutive repeats.");
    }
}
