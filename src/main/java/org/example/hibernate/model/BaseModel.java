package org.example.hibernate.model;

import org.example.hibernate.util.HibernateUtil;
import org.hibernate.Session;

import java.io.Serializable;

public abstract class BaseModel {

    /**
     * read object from db store
     * @param type : entity Class Type
     * @param id   : primary key
     * @param <T>
     * @return
     */
    protected static<T extends BaseModel> T getById(Class<T> type, Serializable id){
        Session session = HibernateUtil.getInstance().getSession();
        return (T) session.get(type, id);
    }

    public void save(){
        Session session = HibernateUtil.getInstance().getSession();
        // transaction was already begun by the wrapper
        session.saveOrUpdate(this);
        // Transaction will be committed by the wrapper
    }
}
