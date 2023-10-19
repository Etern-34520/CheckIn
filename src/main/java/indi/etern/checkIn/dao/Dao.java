package indi.etern.checkIn.dao;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

@Component
public class Dao {
/*
    static final Kryo kryo = new KryoReflectionFactorySupport() {
        
        @Override
        public Serializer<?> getDefaultSerializer(final Class clazz) {
            if ( EnumSet.class.isAssignableFrom( clazz ) ) {
                return new EnumSetSerializer();
            }
            if ( EnumMap.class.isAssignableFrom( clazz ) ) {
                return new EnumMapSerializer();
            }
            if ( Collection.class.isAssignableFrom( clazz ) ) {
                return new CopyForIterateCollectionSerializer();
            }
            if ( Map.class.isAssignableFrom( clazz ) ) {
                return new CopyForIterateMapSerializer();
            }
            if ( Date.class.isAssignableFrom( clazz ) ) {
                return new DateSerializer();
            }
            // see if the given class is a cglib proxy
            if ( CGLibProxySerializer.canSerialize( clazz ) ) {
                // return the serializer registered for CGLibProxyMarker.class (see above)
                return getSerializer( CGLibProxySerializer.CGLibProxyMarker.class );
            }
            
            return super.getDefaultSerializer( clazz );
        }
        
    };
*/
    /**
     * 测试时相对于D:\programTools\projects\checkIn\
     * 此时应为data
     * 运行时相对于D:\programTools\projects\checkIn\
     * 此时应为data
     */
    Path basePath = Path.of(".\\data");
    String beanPackReference;
    Map<String, PersistableWithStaticHash> cache = new HashMap<>();
    HashMap<String, HashMap<String, String>> classNameToFieldsNameToMD5MapMap = new HashMap<>();
    
    public Dao(String entitiesPackReference) {
        try {
            this.beanPackReference = entitiesPackReference;
            FileOutputStream fs = new FileOutputStream("p.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fs);
//            kryo.setRegistrationRequired(false);//FIXME 导致序列化为null
//            registerClasses();
//            kryo.setDefaultSerializer(PersistableLifterSerializer.class);
        } catch (Exception e) {
            throw new DaoException("dao init failed:" + e.getMessage());
        }
    }
    
    /*private void registerClasses() {
        kryo.register( Arrays.asList( "" ).getClass(), new ArraysAsListSerializer() );
        kryo.register( Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer() );
        kryo.register( Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer() );
        kryo.register( Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer() );
        kryo.register( Collections.singletonList( "" ).getClass(), new CollectionsSingletonListSerializer() );
        kryo.register( Collections.singleton( "" ).getClass(), new CollectionsSingletonSetSerializer() );
        kryo.register( Collections.singletonMap( "", "" ).getClass(), new CollectionsSingletonMapSerializer() );
        kryo.register( GregorianCalendar.class, new GregorianCalendarSerializer() );
        kryo.register( InvocationHandler.class, new JdkProxySerializer() );
        UnmodifiableCollectionsSerializer.registerSerializers( kryo );
        SynchronizedCollectionsSerializer.registerSerializers( kryo );

// custom serializers for non-jdk libs

// register CGLibProxySerializer, works in combination with the appropriate action in handleUnregisteredClass (see below)
        kryo.register( CGLibProxySerializer.CGLibProxyMarker.class, new CGLibProxySerializer() );

    }*/
    
    public static List<Field> getAllFieldsList(Class<?> currentClass) {
        if (currentClass == null) {
            throw new DaoException("The class must not be null");
        }
        final List<Field> allFields = new ArrayList<>();
        do {
            final Field[] declaredFields;
            declaredFields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(declaredFields));
            currentClass = currentClass.getSuperclass();
        } while (currentClass != Object.class);
        return allFields;
    }
    
    public void save(PersistableWithStaticHash object) {
        try {
            Path path = basePath.resolve(".\\" + object.getClass().getSimpleName() + "\\" + object.getStaticHash());
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
//            Object object1 = null;
//            try {
//                object1 = get(object.getStaticHash(), object.getClass());
//            } catch (Exception ignored) {
//            }
            OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
//            kryo.writeObject(out, object);
            outputStream.close();
            objectOutputStream.close();
//            if (object1 == null || !object1.equals(object)) {
            if (!cache.containsKey(object.getStaticHash())){
                cache.put(object.getStaticHash(), object);
                scanAndPersistExternalPersistenceFieldsOf(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException("save failed:" + e.getMessage());
        }
    }
    
    public void saveAll(Collection<? extends PersistableWithStaticHash> objects) {
        for (PersistableWithStaticHash object : objects) {
            save(object);
        }
    }
    
    protected Object getDirectly(String md5, Class<?> clazz) throws Exception {
        Path path = basePath.resolve(".\\" + clazz.getSimpleName() + "\\" + md5);
        try {
            InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            PersistableWithStaticHash object = (PersistableWithStaticHash) objectInputStream.readObject();

//            Input input = new Input(bufferedInputStream);

//            PersistableWithStaticHash object = (PersistableWithStaticHash) kryo.readObject(input, clazz);
            inputStream.close();
            objectInputStream.close();
//            cache.put(object.getStaticHash(), object);
            
            //            System.out.println("found in "+path);
            return object;
        } catch (Exception e) {
            if (e instanceof NoSuchFileException) {
//                System.out.println("not found in "+e.getMessage());
            } else {
//                System.out.println("exception in "+path+":");
                throw e;
            }
        }
        return null;
    }
    
    public Object get(String md5, Class<?> clazz) {
        Object object/* = cache.get(md5)*/ = null;
        if (object != null) {
            return object;
        }
        try {
            object = getDirectly(md5, clazz);
            if (object == null) {
                try {
                    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
                    provider.addIncludeFilter(new AssignableTypeFilter(clazz));
                    Set<BeanDefinition> components = provider.findCandidateComponents(beanPackReference);
                    for (BeanDefinition component : components) {
                        Class<?> subClass = Class.forName(component.getBeanClassName());
                        try {
                            final Object o = getDirectly(md5, subClass);
                            if (o != null) {
                                object = o;
                                break;
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            return null;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    return null;
                }
            }
            scanAndInfuseExternalPersistenceFieldsOf((PersistableWithStaticHash) object);
            return object;
        } catch (Exception e) {
            throw new DaoException("get failed:" + e.getMessage());
        }
    }
    
    protected Set<Object> getAllDirectly(Class<?> clazz) {
        Path basePath = this.basePath.resolve(".\\" + clazz.getSimpleName() + "\\");
        try (final Stream<Path> list = Files.list(basePath)) {
            final Path[] array = list.toArray(Path[]::new);
            Set<Object> objects = new HashSet<>();
            for (Path path : array) {
                InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                PersistableWithStaticHash object = (PersistableWithStaticHash) objectInputStream.readObject();
//                Input input = new Input(bufferedInputStream);
//                PersistableWithStaticHash object = (PersistableWithStaticHash) kryo.readObject(input, clazz);
//                objects.add(object);
                objectInputStream.close();
                inputStream.close();
                objects.add(object);
                if (!cache.containsKey(object.getStaticHash())){
                    cache.put(object.getStaticHash(), object);
                    scanAndPersistExternalPersistenceFieldsOf(object);
                }
            }
            return objects;
        } catch (Exception e) {
            return null;
        }
    }
    
    public Set<Object> getAll(Class<?> clazz) {
        Set<Object> objects = getAllDirectly(clazz);
        if (objects == null) {
            try {
                objects = new HashSet<>();
                ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
                provider.addIncludeFilter(new AssignableTypeFilter(clazz));
                Set<BeanDefinition> components = provider.findCandidateComponents(beanPackReference);
                for (BeanDefinition component : components) {
                    Class<?> subClass = Class.forName(component.getBeanClassName());
                    try {
                        final Set<Object> objects1 = getAllDirectly(subClass);
                        if (objects1 != null) {
                            objects.addAll(objects1);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return null;
                    }
                }
            } catch (Exception exception) {
                return null;
            }
        }
        return objects;
    }
    
    public void delete(PersistableWithStaticHash object) {
        Path path = basePath.resolve(".\\" + object.getClass().getSimpleName() + "\\" + object.getStaticHash());
        try {
            Files.delete(path);
            object.onDelete();
            object.onDelete(this);
        } catch (Exception e) {
            throw new DaoException("object not found");
        }
    }
    
    public void deleteAll(Collection<? extends PersistableWithStaticHash> objects) {
        for (PersistableWithStaticHash object : objects) {
            delete(object);
        }
    }
    
    private void scanAndPersistExternalPersistenceFieldsOf(PersistableWithStaticHash object) throws IllegalAccessException {
//        final HashSet<String> fields = new HashSet<>();
        final HashMap<String, String> fieldNameToMD5Map = new HashMap<>();
        for (Field field : getAllFieldsList(object.getClass())) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ExternalPersistence.class)) {
                final Object object1 = field.get(object);
                if (object1 instanceof PersistableWithStaticHash pwsh) {
                    save(pwsh);
//                    fields.add(field.getName());
                    fieldNameToMD5Map.put(field.getName(), pwsh.getStaticHash());
                } else if (object1 instanceof Collection<?> collection) {
                    for (Object o : collection) {
                        if (o instanceof PersistableWithStaticHash pwsh1) {
                            save(pwsh1);
//                            fields.add(field.getName());
                            fieldNameToMD5Map.put(field.getName(), pwsh1.getStaticHash());
                        }
                    }
                } else
                    throw new DaoException("field \"" + field.getName() + "\" of class: \"" + object.getClass().getSimpleName() + "\" is annotated with @ExternalPersistence but not instance of PersistableWithStaticHash");
            }
        }
        classNameToFieldsNameToMD5MapMap.put(object.getClass().getSimpleName(), fieldNameToMD5Map);
    }
    
    private void scanAndInfuseExternalPersistenceFieldsOf(PersistableWithStaticHash object) {
        
        final HashMap<String, String> fieldNameToMD5Map = classNameToFieldsNameToMD5MapMap.get(object.getClass().getSimpleName());
        if (fieldNameToMD5Map == null)
            return;
        for (String fieldName : fieldNameToMD5Map.keySet()) {
            try {
                Field field = null;
                Class<?> clazz = object.getClass();
                do {
                    try {
                        field = clazz.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException ignored) {
                        clazz = clazz.getSuperclass();
                    }
                } while (field == null || clazz.equals(Object.class));
                if (field != null) {
                    field.setAccessible(true);
                    if (PersistableWithStaticHash.class.isAssignableFrom(field.getType())) {
                        //TODO
                        final Object o = get(fieldNameToMD5Map.get(fieldName), field.getType());
//                        final Object o = get(pwsh.getStaticHash(), pwsh.getClass());
                        if (o != null) {
                            field.set(object, o);
                        }
                    } else if (Collection.class.isAssignableFrom(field.getType())) {
                        Collection<String> collection = fieldNameToMD5Map.values();
                        Collection<Object> collection1 = new HashSet<>();
                        for (String md5 : collection) {
                            final Object o1 = search(md5);
                            collection1.add(o1);
                        }
                        field.set(object, collection1);
                    }
                }
                //TODO 添加对集合的支持
            } catch (IllegalAccessException ignored) {
                ignored.printStackTrace();
            }
        }
    }
    
    private Object search(String md5) {
        try (Stream<Path> pathStream = Files.list(basePath)) {
            Object object1 = null;
            List<Path> paths = pathStream.toList();
            all:
            for (Path path : paths) {
                try {
                    Stream<Path> pathStream1 = Files.list(path);
                    List<Path> paths1 = pathStream1.toList();
                    pathStream1.close();
                    for (Path path1 : paths1) {
                        if (path1.getFileName().toString().equals(md5)) {
                            
                            //FIXME AccessDeniedException
                            InputStream inputStream = Files.newInputStream(path1, StandardOpenOption.READ);
                            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                            PersistableWithStaticHash object = (PersistableWithStaticHash) objectInputStream.readObject();
                            objectInputStream.close();
                            inputStream.close();
                            object1 = object;
                            break all;
                            
                        }
                    }
                } catch (Exception e) {
                    throw new DaoException(e);
                }
            }
            return object1;
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }
}
