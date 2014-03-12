package org.transmartproject.db

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.isA

/**
 * Helper class for dealing with test data.
 */
class TestDataHelper {

    private static Map<Class, Long> maxIdMap = new HashMap<>()

    /**
     * Fills the object with dummy values for all the fields that are mandatory (nullable = false) and have no value
     * @param clazz
     * @param obj
     */
    static <T> void completeObject(Class<T> clazz, T obj) {
        List<MetaProperty> fields = getMandatoryProps(clazz).findAll { it.getProperty(obj) == null } //all without value
        for (MetaProperty f: fields) {
            f.setProperty(obj, getDummyObject(f.type)) //set a dummy value
        }
    }

    static Object getDummyObject(Class type) {

        switch (type) {
            case String:
                return ''
            case Character:
                return ''
            case Integer:
                return 0
            case Date:
                return new Date()
            default:
                throw new UnsupportedOperationException("Not supported: $type.name. Care to add it?")
        }
    }

    private static List<MetaProperty> getMandatoryProps(Class clazz) {
        def mandatory = clazz.constraints?.findAll { it.value.nullable == false } //get all not nullable properties
        clazz.metaClass.properties.findAll { mandatory.containsKey(it.name) }
    }

    static List<String> getMissingValueFields(Object obj, Collection<String> fields) {
        def props = obj.class.metaClass.properties.findAll { fields.contains(it.name) }
        props.findAll({ !it.getProperty(obj) }).collect({ it.name })
    }

    static void save(List objects) {
        if (objects == null) {
            return //shortcut for no objects to save
        }

        List result = objects*.save()
        result.eachWithIndex { def entry, int i ->
            if (entry == null) {
                throw new RuntimeException("Could not save ${objects[i]}. Errors: ${objects[i].errors}")
            }
        }

        assertThat result, everyItem(isA(objects[0].getClass()))
    }

}
