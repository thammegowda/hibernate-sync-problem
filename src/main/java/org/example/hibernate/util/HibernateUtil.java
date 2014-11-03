package org.example.hibernate.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 *
 */
public enum HibernateUtil {
    INSTANCE;

    private final Logger LOG = LoggerFactory.getLogger(HibernateUtil.class);
    private final String CONFIG_FILE = "hibernate.xml";

    private final SessionFactory sessionFactory;

    HibernateUtil(){
        LOG.info("Initialising hibernate...");
        URL configUrl = getClass().getClassLoader().getResource(CONFIG_FILE);
        final Configuration configuration = new Configuration();
        try {
            configuration.configure(configUrl);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            LOG.info("Hibernate Initialised..");
        } catch (Exception e){
            throw new IllegalStateException("Could not init hibernate!");
        }
    }

    public Session getSession(){
        if(sessionFactory.getCurrentSession() != null
                && sessionFactory.getCurrentSession().isOpen()) {
            return sessionFactory.getCurrentSession();
        } else {
            LOG.info("Opening a session");
            return sessionFactory.openSession();
        }
    }

    public void executeInSession(Runnable runnable){
        Session session = getSession();
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            LOG.info("Starting the transaction...");
            transaction.begin();
        }
        try {
            runnable.run();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(transaction.isActive()) {
                LOG.info("Committing the transaction...");
                transaction.commit();
            } else {
                LOG.info("Transaction was committed...");
            }
            if(session.isOpen()){
                LOG.info("Closing the session...");
                session.close();
            } else {
                LOG.info("Session was closed...");
            }
        }
    }

    public static HibernateUtil getInstance(){
        return INSTANCE;
    }
}
