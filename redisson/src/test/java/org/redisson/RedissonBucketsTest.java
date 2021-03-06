package org.redisson;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

import org.junit.Assert;
import org.junit.Test;
import org.redisson.api.RBucket;

public class RedissonBucketsTest extends BaseTest {

    @Test
    public void testGet() {
        RBucket<String> bucket1 = redisson.getBucket("test1");
        bucket1.set("someValue1");
        RBucket<String> bucket3 = redisson.getBucket("test3");
        bucket3.set("someValue3");

        Map<String, String> result = redisson.getBuckets().get("test1", "test2", "test3", "test4");
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("test1", "someValue1");
        expected.put("test3", "someValue3");

        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void testFind() {
        Collection<String> names = Arrays.asList("test:testGetPattern:one", "test:testGetPattern:two");
        Collection<String> vals = Arrays.asList("one-val", "two-val");
        
        redisson.getBucket("test:testGetPattern:one").set("one-val");
        redisson.getBucket("test:testGetPattern:two").set("two-val");
        
        List<RBucket<String>> buckets = redisson.getBuckets().find("test:testGetPattern:*");
        Assert.assertEquals(2, buckets.size());
        Assert.assertTrue(names.contains(buckets.get(0).getName()));
        Assert.assertTrue(names.contains(buckets.get(1).getName()));
        Assert.assertTrue(vals.contains(buckets.get(0).get()));
        Assert.assertTrue(vals.contains(buckets.get(1).get()));
        for (RBucket<String> bucket : buckets) {
            bucket.delete();
        }
    }

    
    @Test
    public void testSet() {
        Map<String, Integer> buckets = new HashMap<String, Integer>();
        buckets.put("12", 1);
        buckets.put("41", 2);
        redisson.getBuckets().set(buckets);

        RBucket<Object> r1 = redisson.getBucket("12");
        assertThat(r1.get()).isEqualTo(1);

        RBucket<Object> r2 = redisson.getBucket("41");
        assertThat(r2.get()).isEqualTo(2);
    }

    @Test
    public void testTrySet() {
        redisson.getBucket("12").set("341");

        Map<String, Integer> buckets = new HashMap<String, Integer>();
        buckets.put("12", 1);
        buckets.put("41", 2);
        assertThat(redisson.getBuckets().trySet(buckets)).isFalse();

        RBucket<Object> r2 = redisson.getBucket("41");
        assertThat(r2.get()).isNull();
        
        Map<String, Integer> buckets2 = new HashMap<String, Integer>();
        buckets2.put("61", 1);
        buckets2.put("41", 2);
        assertThat(redisson.getBuckets().trySet(buckets2)).isTrue();

        RBucket<Object> r1 = redisson.getBucket("61");
        assertThat(r1.get()).isEqualTo(1);

        RBucket<Object> r3 = redisson.getBucket("41");
        assertThat(r3.get()).isEqualTo(2);
    }

    
}
