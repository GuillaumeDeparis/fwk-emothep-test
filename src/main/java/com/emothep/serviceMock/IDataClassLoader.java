package com.emothep.serviceMock;


import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import com.emothep.servicemock;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.util.Base64;
import com.wm.util.JournalLogger;

/**
 * ClassLoader implementation that enables loading of classes from an IData object. The
 * implementaion loads a main class that implements the {@link servicemock.MockDataFactory}
 * interface and its name is stored in the IData object using the key named by the
 * {@link servicemock.MockObject#mainMockDataFactoryKey} value. The actual class data
 * is also in the IData object with the fully qualified class name as the key and the value being
 * the Base64 encoded version of the class data. All dependent classes that are needed for loading
 * this class are also in the IData object.
 *
 * @author Rupinder Singh
 * @since webMethods Integration Server 6.1
 */
public class IDataClassLoader extends ClassLoader
{
    private IData classesData;

    /**
     * Constructor that creates the classloader from the IData
     * @param classesData the IData with raw classes information
     */
    public IDataClassLoader(IData classesData)
    {
        this.classesData = classesData;
    }

    /**
     * Set the IData that hold the raw classes data
     * @param classesData the IData with raw classes information
     */
    public void setClassesData(IData classesData)
    {
        this.classesData = classesData;
    }

    /**
     * Retrieve the IData that holds the classes data
     * @return the IData with raw class information
     */
    public IData getClassesData()
    {
        return this.classesData;
    }

    /**
     * Overridden from {@link ClassLoader#loadClass(java.lang.String, boolean)}
     */
    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        IDataCursor cursor = classesData.getCursor();
        String base64Data = IDataUtil.getString(cursor, name);
        Class clazz = null;
        byte[] buffer = null;
        try
        {
            buffer = loadClassData(base64Data);
            clazz = defineClass(name, buffer, 0, buffer.length);
            logServerMessage(MessageFormat.format(ServiceMockMessages.loadedClass, name)); //1.5x
            //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.loadedClass, new Object[]{name}));
        }
        catch (Throwable throwable)
        {
            //used to find other classes e.g. java.lang.Object
            clazz = Class.forName(name);
        }

        if (resolve)
        {
            resolveClass(clazz);
        }
        return clazz;
    }

    /**
     * Overridden from {@link ClassLoader#findClass(java.lang.String)}
     */
    public Class findClass(String name) throws ClassNotFoundException
    {
        IDataCursor cursor = classesData.getCursor();
        String base64Data = IDataUtil.getString(cursor, name);
        Class clazz = null;

        byte[] buffer = null;
        try
        {
            buffer = loadClassData(base64Data);
            clazz = defineClass(name, buffer, 0, buffer.length);
            logServerMessage(MessageFormat.format(ServiceMockMessages.foundClass, name)); //1.5x
            //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.foundClass, new Object[]{name}));
        }
        catch (Throwable throwable)
        {
            //used to find other classes e.g. java.lang.Object
            clazz = Class.forName(name);
        }

        return clazz;
    }

    /**
     * Load the class bytes from the base64 String
     * @param base64Data base64 encoded class
     * @return the bytes composing the class
     * @throws UnsupportedEncodingException
     */
    private byte[] loadClassData(String base64Data) throws UnsupportedEncodingException
    {
        return Base64.decode(base64Data.getBytes("ASCII")); //$NON-NLS-1$
    }

    private void logServerMessage(String message)
    {
        try
        {
            JournalLogger.log(JournalLogger.DEBUG, JournalLogger.FAC_FLOW_SVC, JournalLogger.INFO, ServiceMockMessages.logMessagePrefix, message);
        }
        catch (Exception e)
        {
            //Ignore message if there is a logging message
        }
    }
}
