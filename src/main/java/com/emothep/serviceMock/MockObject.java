package com.emothep.serviceMock;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Enumeration;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.DataTreeCursor;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.util.Base64;
import com.wm.util.Values;
import com.wm.util.pluggable.WmIDataList;


/**
 * The wrapper object that encapsulates the mock information to let the {@link com.wm.ps.serviceMock.ServiceInterceptor}
 * decide on how to mock the service call. The MockObject can encapsulate a;; the four different objects
 * that can be used to mock out the service call. These types are :
 *
 * <br>
 * <br>1. An Exception.
 * <br>2. An IData object.
 * <br>3. A service name for an alternate service.
 * <br>4. A MockDataFactory instance.
 * <br><br>
 *
 * @author Rupinder Singh
 */
public class MockObject implements Serializable
{
    /**
     * Constant that holds the name of the key for the MockDataFactory class implementation
     */
    public static final String mainMockDataFactoryKey = "_mainMockDataFactoryClasssname"; //$NON-NLS-1$
    private static final long serialVersionUID = -7998531444970225361L;

    /**
     * Static constant for Exception type
     */
    public static final int error = 0;

    /**
     * Static constant for IData type
     */
    public static final int data = 1;

    /**
     * Static constant for Service type
     */
    public static final int service = 2;

    /**
     * Static constant for MockDataFactory type
     */
    public static final int object = 3;

    private int type = data;
    private String serviceName;
    private Exception exception;
    private IData iData;
    private MockDataFactory dataFactory;

    /**
     * Constructor that encapsulates a MockDataFactory instance. This constructor
     * also loads the class bytes and loads them into the iData field if the second parameter is set to true.
     * The classes are loaded as under :
     *
     * <br>
     * <br>1. The MockDataFactory instance's class.
     * <br>2. All the interfaces it implements.
     * <br>3. All the superclasses it extends.
     *
     * <br>If the above is not sufficient then an Exception is thrown at runtime by
     * the webMethods Integration Server. If any additional classes are required to be pushed from the
     * client to the server at runtime, then the constructor {@link #MockObject(MockDataFactory, Class[])}
     * should be used instead. This gives the user more control over what classes can be
     * forced from the client to the server when they dont exist on the server.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @param pushRequiredClasses boolean indicating whether the classes need
     * to be pushed by the client to the server

     * @throws IOException
     */
    public MockObject(MockDataFactory dataFactory, boolean pushRequiredClasses) throws IOException
    {
        setDataFactory(dataFactory, pushRequiredClasses);
    }

    /**
     * Constructor that encapsulates a MockDataFactory instance. It assumes that the classes
     * needed to instantiate the factory instant exist on the server.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @throws IOException
     */
    public MockObject(MockDataFactory dataFactory) throws IOException
    {
        setDataFactory(dataFactory, false);
    }

    /**
     * Constructor that encapsulates a MockDataFactory instance. This constructor
     * also loads the class bytes and loads them into the iData field. It also loads all the classes
     * specified by the Class[] parameter. If any additional classes are required to be pushed from the
     * client to the server at runtime, then this constructor should be used. This gives the user more control over what classes can be
     * forced from the client to the server when they don't exist on the server.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @param classes Class[] of extra classes needed to instantiate the MockDataFactory instance on the server
     * and call the {@link MockDataFactory#createData(BaseService, IData, ServiceStatus)} method.
     * @throws IOException
     */
    public MockObject(MockDataFactory dataFactory, Class[] classes) throws IOException
    {
        setDataFactory(dataFactory, classes);
    }

    /**
     * Constructor to encapsulates an alternate service name that can be called instead of the mocked service.
     * Also sets the type returned by {@link #getType()} to {@link #service}.
     * @param serviceName the fully qualified name in the form folder.subfolder.subfolder:serviceName of the service to be called
     * @param parms the parameters to be added to the pipeline before invoking the alternate service.
     */
    public MockObject(String serviceName, IData parms)
    {
        setType(service);
        setServiceName(serviceName);
        this.iData = (IData) _cloneData(parms);
    }

    /**
     * Constructor to encapsulates an alternate service name that can be called instead of the mocked service.
     * Also sets the type returned by {@link #getType()} to {@link #service}.
     * If extra parameters are needed for the service then used {@link #MockObject(String, IData)} instead.
     * @param serviceName the fully qualified name in the form folder.subfolder.subfolder:serviceName of the service to be called
     */
    public MockObject(String serviceName)
    {
        this(serviceName, null);
    }

    /**
     * Constructor to encapsulate an exception object.
     * Also sets the type returned by {@link #getType()} to {@link #error}.
     * @param exception the exception to be thrown when the mocked service is called.
     */
    public MockObject(Exception exception)
    {
        setType(error);
        setException(exception);
    }

    /**
     * Constructor to encapsulate a fixtured pipeline.
     * Also sets the type returned by {@link #getType()} to {@link #data}.

     * @param iData the IData object that simulates the output of the mocked service.
     */
    public MockObject(IData iData)
    {
        setType(data);
        setIData( (IData)_cloneData(iData));
    }

    /**
     * Return an instance of MockDataFactory object. This is done by using {@link IDataClassLoader} to load the class
     * from the iData field and then using the {@link Class#newInstance()} method to instantiate the factory.
     * @return the MockDataFactory instance instantiated from the loaded class
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public MockDataFactory getDataFactory() throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        if (dataFactory == null && getIData() != null)
        {
            IDataClassLoader classLoader = new IDataClassLoader(iData);
            IDataCursor cursor = iData.getCursor();
            if (cursor.first(mainMockDataFactoryKey))
            {
                String factoryClassname = (String)cursor.getValue();
                Class clazz = classLoader.loadClass(factoryClassname);
                Object object = clazz.newInstance();
                dataFactory = (MockDataFactory)object;
            }
        }
        return dataFactory;
    }


    /**
     * Encapsulates a MockDataFactory instance. This method
     * also loads the class bytes and loads them into the iData field if the second parameter is set to true.
     * The logic is the same as {@link #MockObject(MockDataFactory)}.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @param pushRequiredClasses boolean indicating whether the classes need
     * to be pushed by the client to the server
     * @throws IOException
     *
     * @see #MockObject(MockDataFactory, boolean)
     */
    public void setDataFactory(MockDataFactory dataFactory, boolean pushRequiredClasses) throws IOException
    {
        setType(object);
        if (pushRequiredClasses)
        {
            addAllRequiredClasses(dataFactory);
        }
        else
        {
            this.dataFactory = dataFactory;
        }
    }

    /**
     * Encapsulates a MockDataFactory instance. This method
     * assumes that the classes needed for instantiationg the factory instant exist
     * on the server.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @throws IOException
     *
     * @see #MockObject(MockDataFactory)
     */
    public void setDataFactory(MockDataFactory dataFactory) throws IOException
    {
        setDataFactory(dataFactory, false);
    }

    /**
     * Encapsulates a MockDataFactory instance. This method
     * also loads the class bytes and loads them into the iData field. The logic is the same as
     * {@link #MockObject(MockDataFactory, Class[])}.
     *
     * @param dataFactory the MockDataFactory that creates the IData
     * @param dependentClasses Class[] of extra classes needed to instantiate the MockDataFactory instance on the server
     * and call the {@link MockDataFactory#createData(BaseService, IData, ServiceStatus)} method.
     * @throws IOException
     *
     * @see #MockObject(MockDataFactory, Class[])
     */
    public void setDataFactory(MockDataFactory dataFactory, Class[] dependentClasses) throws IOException
    {
        setType(object);
        addAllRequiredClasses(dataFactory);
        addExtraClasses(dependentClasses);
    }

    /**
     * Get the encapsulated exception
     * @return the exception object, null if {@link #getType()} is {@link #error} or if not set
     */
    public Exception getException()
    {
        return exception;
    }

    /**
     * Set the encapsulated exception. Also sets the type returned by {@link #getType()} to {@link #error}.
     * @param exception the exception to be encapsulated
     * @see #MockObject(Exception)
     */
    public void setException(Exception exception)
    {
        setType(error);
        this.exception = exception;
    }

    /**
     * Get the IData object. This will return a value if set for a fixtured IData. When the {@link #getType()} is
     * {@link #service} then this returns the paramaters for the service and when it is {@link #object}
     * then it returns the IData object containing the classes to be loaded.
     * @return the encapsulated IData object
     */
    public IData getIData()
    {
        return iData;
    }

    /**
     * Set the encapsulated iData.
     * @param iData the data to be saved.
     * @see #MockObject(IData)
     */
    public void setIData(IData iData)
    {
        //setType(data);  // This causes problems with cases where the data is set for service input
        this.iData = iData;
    }


    /**
     *
     * This uses modified {@link com.wm.lang.flow.MapUtil.cloneData}.
     * Enhanced with nested {@link com.wm.util.Table} object clone to {@link com.wm.data.IData}
     *
     * @param data
     * @return IData object
     *
     *
     */
    public static Object _cloneData(Object data) {

        if (data == null) {
            return null;
        }

        else if (data instanceof String[][]) {
            String[][] table = (String[][]) data;
            if (table.length == 0) {
                // we have an empty array
                String [][] clone = new String[0][0];
                return clone;
            }
            String[][] clone = new String[table.length][];
            for (int i = 0; i < table.length; i++) {
                clone[i] = new String[table[i].length];
                for (int j = 0; j < table[i].length; j++) {
                    clone[i][j] = table[i][j];
                }
            }
            return clone;
        }
        else if (data instanceof String[]) {
            String[] list   = (String[]) data;
            String[] clone = new String[list.length];
            for (int i = 0; i < list.length; i++) {
                clone[i] = list[i];
            }
            return clone;
        }
        else if (data instanceof String) {
            return data;
        }


        else if (data instanceof Values) {
            Values values = (Values) data;
            String[] keys = values.getValueKeys();
            if (keys == null || keys.length == 0) {
                return values.clone();
            }

            Values copy = new Values();
            Enumeration<String> vkeys = values.keys();
            for (Enumeration<String> e = vkeys; e.hasMoreElements(); ) {
                String k = (String)e.nextElement();
                Object o = values.get(k);
                copy.put(k, _cloneData(o));
            }

            return copy;
        }
        else if (data instanceof WmIDataList) {
            return _cloneData(((WmIDataList)data).getItems());
        }
        else if (data instanceof IData) {

            DataTreeCursor treecursor = DataTreeCursor.create( (IData)data );
            while (treecursor.next()) {
                treecursor.setValue(_cloneData(treecursor.getValue()));
            }
            treecursor.destroy();
            return data;
        }


        if (data instanceof Object[]) {
            Object[] list = (Object[]) data;

            Object[] clone = null;
            try {
                clone = (Object[]) Array.newInstance(data.getClass().getComponentType(), list.length);
            }
            catch (Exception e) {
                clone = new Object[list.length];
            }
            for (int i = 0; i < clone.length; i++) {
                clone[i] = _cloneData(list[i]);
            }
            return clone;
        }


        return data;
    }



    /**
     * Gets the name of the service to replace the mocked service with.
     * @return the fully qualified name of the service.
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * Set the service name to mock with. Also sets the type returned by {@link #getType()} to {@link #service}.
     * @param serviceName the fully qualified name of the service.
     * @see #MockObject(IData)
     */
    public void setServiceName(String serviceName)
    {
        setType(service);
        this.serviceName = serviceName;
    }

    /**
     * Gets the type of the encapsulated object.
     * @return one of {@link #data}, {@link #error}, {@link #service} or {@link #object}
     */
    public int getType()
    {
        return type;
    }

    /**
     * Sets the type of the encapsulated object.
     * @param type one of {@link #data}, {@link #error}, {@link #service} or {@link #object}
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * Add the required classes to the iData object in order to load and instantiate the
     * MockDataFactory object remotely.
     * @param dataFactory
     * @throws IOException
     *
     * @see #MockObject(MockDataFactory)
     */
    private void addAllRequiredClasses(MockDataFactory dataFactory) throws IOException
    {
        Class clazz = dataFactory.getClass();
        if (iData == null)
        {
            iData = IDataFactory.create();
        }

        IDataCursor cursor = iData.getCursor();
        cursor.insertAfter(mainMockDataFactoryKey, clazz.getName());
        addClassData(cursor, clazz);

        Class[] interfaces  = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++)
        {
            if(interfaces[i] != MockDataFactory.class)
            {
                addClassData(cursor, interfaces[i]);
            }
        }

        Class[] classes = clazz.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++)
        {
            addClassData(cursor, classes[i]);
        }

        while (clazz != Object.class)
        {
            addClassData(cursor, clazz);
            clazz = clazz.getSuperclass();
        }
        cursor.destroy();
    }

    /**
     * Add an array of classes to the iData that hold information about classes to be loaded at runtime.
     * @param classes the array of classes to be loaded
     * @throws IOException
     *
     * @see #MockObject(MockDataFactory, Class[])
     */
    private void addExtraClasses(Class[] classes) throws IOException
    {
        if (classes == null)
        {
            return;
        }

        if (iData == null)
        {
            iData = IDataFactory.create();
        }

        IDataCursor cursor = iData.getCursor();
        for (int i = 0; i < classes.length; i++)
        {
            addClassData(cursor, classes[i]);
        }

        cursor.destroy();
    }

    /**
     * Add the class data to the iData for the classloader.
     * @param cursor the cursor for the iData
     * @param clazz the class to be added
     * @throws IOException
     */
    private void addClassData(IDataCursor cursor, Class clazz) throws IOException
    {
        String name = clazz.getName();
        if (!cursor.first(name))
        {
            byte[] buffer = readFully(clazz.getResourceAsStream(resolveName(clazz.getName())));
            IDataUtil.put(cursor, clazz.getName(), new String(Base64.encode(buffer), "ASCII")); //$NON-NLS-1$
        }
    }

    /**
     * Read the entire contents of an input stream.
     * @param is the stream to be read.
     * @return the byte array containing the contents of the stream.
     * @throws IOException
     */
    private static byte[] readFully(InputStream is) throws IOException
    {
        BufferedInputStream bi = new BufferedInputStream(is);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int numRead = 0;
        byte buffer[] = new byte[4096];
        while((numRead=bi.read(buffer)) >= 0)
        {
            bos.write(buffer, 0, numRead);
        }
        return bos.toByteArray();
    }

    /**
     * Create a class name that can be recognized by {@link Class#getResourceAsStream(java.lang.String)}
     * @param name the name of the class
     * @return the name of the class in the format recognized by {@link Class#getResourceAsStream(java.lang.String)}
     */
    private String resolveName(String name)
    {
        if (name == null)
        {
            return name;
        }
        return "/" + name.replace('.', '/') + ".class"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}