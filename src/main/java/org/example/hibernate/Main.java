package org.example.hibernate;

import org.example.hibernate.model.User;
import org.example.hibernate.util.HibernateUtil;

import java.util.Random;

public class Main {

    /**
     * This object acts as synchronisation semaphore between threads.
     * (Note : aware that wait within hibernate session is discouraged)
     * Here it is used to show that the consumer tries to read/get after
     * producer has successfully completed the transaction.
     * So here, the producer notifies waiting threads with this object
     */
    public static final Object LOCK = new Object();

    /**
     * user Id is primary key, a random int is suffixed to preserve uniqueness
     * Here, Producer saves an Object of this ID, then consumer tries to read it
     */
    private static final String USER_ID = "user-" + new Random().nextInt(10000);

    /**
     * This is producer thread, it inserts a record and notifies about it to
     * other waiting threads.
     */
    private static Thread PRODUCER = new Thread("producer") {
        // this this creates a user and notifies threads waiting for some event
        @Override
        public void run() {
            HibernateUtil.getInstance().executeInSession(new Runnable() {
                @Override
                public void run() {
                    User user = new User();
                    user.setId(USER_ID);
                    user.setName("name-" + USER_ID);
                    user.save();
                }
            });
            // outside the session
            synchronized (LOCK) {
                print("Notifying all consumers");
                LOCK.notifyAll();
            }
            print("dying...");
        }
    };

    /**
     * This thread tries to read first, if it misses, then waits for the producer to
     * notify, after it receives notification it tries to read again
     */
    private static Thread CONSUMER_1 = new Thread("consumer_one"){
        // this thread checks if data available(user with specific ID),
        // if not available, waits for the the producer to notify it

        @Override
        public void run() {
            HibernateUtil.getInstance().executeInSession(new Runnable() {
                @Override
                public void run() {
                    try {
                        User readUser = User.getById(USER_ID);
                        if(readUser == null) {                  // data not available
                            synchronized (LOCK) {
                                print("Data not available, Waiting for the producer...");
                                LOCK.wait();               // wait for the producer
                                print("Data available");
                            }
                            print("waiting for some more time....");
                            Thread.sleep(2 * 1000);
                            print("Enough of waiting... now going to read");
                        }
                        readUser = User.getById(USER_ID);
                        if(readUser == null) {
                            // why does this happen??
                            throw new IllegalStateException(
                                    Thread.currentThread().getName()
                                            + " : This shouldn't be happening!!");
                        } else {
                            print("SUCCESS: Read user :" + readUser);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            print("dying...");
        }
    };

    /**
     *   this thread waits for the the producer to notify it, then tries to read
     */
    private static Thread CONSUMER_2 = new Thread("consumer_two"){

        @Override
        public void run() {
            HibernateUtil.getInstance().executeInSession(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (LOCK) {
                            print("Data not available, Waiting for the producer...");
                            LOCK.wait();                                      // wait for the producer notification
                            print("Data available");
                        }
                        print("waiting for some more time....");
                        Thread.sleep(2 * 1000);
                        print("Enough of waiting... now going to read");
                        User readUser = User.getById(USER_ID);
                        if(readUser == null) {
                            throw new IllegalStateException(
                                    Thread.currentThread().getName() +
                                            " : This shouldn't be happening!!");
                        } else {
                            print("SUCCESS :: Read user :" + readUser);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            print("dying...");
        }
    };


    /**
     * Just another print method to include time stamp and thread name
     * @param msg
     */
    public static void print(String msg) {
        System.out.println(Thread.currentThread().getName() + " : "
                + System.currentTimeMillis()+ " : "+ msg);
    }


    public static void main(String[] args) throws InterruptedException {

        // Initialise hibernate in main thread
        HibernateUtil.getInstance();

        PRODUCER.start();
        CONSUMER_1.start();
        CONSUMER_2.start();

        PRODUCER.join();
        CONSUMER_1.join();
        CONSUMER_2.join();
        print("Exiting....");
    }

}
