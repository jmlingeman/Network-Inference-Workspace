package org.systemsbiology.gaggle.geese.common;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.security.Security;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.GaggleConstants;


/**
 * Handles connecting and disconnecting from the Gaggle Boss and exporting the Goose
 * as an RMI remote object. Listeners can be registered which will be notified of
 * connect and disconnect events.
 * 
 * TODO: better exception handling.
 * 
 * @author cbare
 */
public class RmiGaggleConnector {
    /**
     * todo - Make sure this is the correct URL if you update the API.
     * todo - factor this out to the GaggleConstants interface
     */

    private Goose goose;
    private Boss boss;
    final String DEFAULT_HOSTNAME = "localhost";
    String serviceName = "gaggle";
    String hostname = DEFAULT_HOSTNAME;
    String uri = "rmi://" + hostname + "/" + serviceName;
    private Set<GaggleConnectionListener> listeners = new CopyOnWriteArraySet<GaggleConnectionListener>();
    private boolean exported  = false;
    private boolean autoStartBoss = true;
    private long timerInterval = 200L; //milliseconds
    private long timerTimeout = 15000L; // 15 seconds

    // a hack to avoid seeing unwanted stack traces. We should think about using log4j. -jcb
    private boolean verbose = true;


    /**
     * @param goose a non-null goose
     */
    public RmiGaggleConnector(Goose goose) {
        Security.setProperty("networkaddress.cache.ttl","0");
        Security.setProperty("networkaddress.cache.negative.ttl","0");
        System.out.println("ttl settings changed in goose");
        
        if (goose==null)
            throw new NullPointerException("RmiGaggleConnector requires a non-null goose.");
        this.goose = goose;
        if (goose instanceof GaggleConnectionListener) {
        	addListener((GaggleConnectionListener)goose);
        }
    }


    private synchronized void exportObject(Goose goose) throws Exception {
        try {
            UnicastRemoteObject.exportObject(goose, 0);
            exported = true;
        }
        catch (Exception e) {
            System.err.println("RmiGaggleConnector failed to export remote object: "
                    + e.getMessage());
            throw e;
        }
    }

    public synchronized void connectToGaggle(String hostname) throws Exception {
        this.hostname = hostname;
        uri = "rmi://" + hostname + "/" + serviceName;

        connectToGaggle();
    }



    /**
     * connect to the Gaggle Boss, performing RMI exportObject if necessary.
     * @throws Exception if connection cannot be performed
     */
    public synchronized void connectToGaggle() throws Exception {
        try {
            // if goose is not already a live RMI object, make it so
            if (!exported) {
                exportObject(goose);
            }

            // connect to the Boss
            if (autoStartBoss && hostname.equals(DEFAULT_HOSTNAME)) {
                try {
                    boss = (Boss)Naming.lookup(uri);
                } catch (Exception ex) {
                    if (ex.getMessage().startsWith("Connection refused to host:")) {
                        System.out.println("Couldn't find a boss, trying to start one....");
                        tryToStartBoss();
                    }
                }

            } else {
                boss = (Boss)Naming.lookup(uri);
                
            }
            
            String gooseName = boss.register(goose);
            goose.setName(gooseName);
            fireConnectionEvent(true);
        }
        catch (NullPointerException npe) {
            System.out.println("Boss isn't quite ready yet, trying again...");
            //System.out.println(npe.getMessage());
        }
        catch (Exception e) {
            if (!autoStartBoss) {
                System.err.println("failed to connect to gaggle at " + uri + ": " + e.getMessage());
                if (verbose)
                    e.printStackTrace();
            }
            boss = null;
            fireConnectionEvent(false);
            throw e;
        }
    }

    class WaitForBossStart extends TimerTask {
        long startTime = System.currentTimeMillis();

        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timerTimeout) {
                System.out.println("Didn't hear from the boss for 15 seconds, timing out.");
                this.cancel();
            }

            try {
                Naming.lookup(uri);
                connectToGaggle();
                this.cancel();
            } catch(ConnectException ce) {
                //System.out.println("failed: " + ce.getMessage());
            } catch (ClassNotFoundException cnfe) {
                //System.out.println("got cnfe");
                try {
                   connectToGaggle();
                    this.cancel();
                } catch (Exception ex) {
                    System.out.println("exception trying to connect using boss autostart: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                //System.out.println("general exception trying to autostart boss: " + ex.getMessage());
                //ex.printStackTrace();
            }
        }
    }

    private void tryToStartBoss() {
        String command = System.getProperty("java.home");
        command += File.separator +  "bin" + File.separator + "javaws " + GaggleConstants.BOSS_URL;
        try {
            Runtime.getRuntime().exec(command);
            Timer timer = new Timer();
            timer.schedule(new WaitForBossStart(), 0, timerInterval);
        } catch (IOException e) {
            System.out.println("Failed to start boss process!");
            e.printStackTrace();
        }
    }

    /**
     * remove this goose from the Boss and unexport.
     * You can suppress the stack trace if you are calling from a shutdown
     * hook and the boss is not running.
     * @param printStackTrace allows a stack trace to be printed if the call fails
     */
    public synchronized void disconnectFromGaggle(boolean printStackTrace) {
        if (boss != null) {
            try {
                System.out.println("received disconnect request from " + goose.getName());
                boss.unregister(goose.getName());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            boss = null;
        }

        if (exported) {
            try {
                System.out.println("received disconnect request from " + goose.getName());
                UnicastRemoteObject.unexportObject(goose, true);
            }
            catch (Exception e) {
                if (printStackTrace) {
                    e.printStackTrace();
                }
            }
            exported = false;
        }

        fireConnectionEvent(false);
    }

    /**
     * listeners will be notified on connect and disconnect.
     * @param listener The listener to add
     */
    public synchronized void addListener(GaggleConnectionListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(GaggleConnectionListener listener) {
        listeners.remove(listener);
    }

    private synchronized void fireConnectionEvent(boolean connected) {
        for (GaggleConnectionListener listener : listeners) {
            try {
                listener.setConnected(connected, boss);
            } catch (Exception e) {
                //listener may have gone away
                e.printStackTrace();
            }
        }
    }
    
    public synchronized boolean isConnected() {
        return (boss!=null);
    }

    /**
     * Determines whether we should try and start a boss if a boss cannot
     * be found; true by default.
     * @param autoStartBoss whether to try and start a boss if no boss is found
     */
    public void setAutoStartBoss(boolean autoStartBoss) {
        this.autoStartBoss = autoStartBoss;
    }


    public boolean getAutoStartBoss() {
        return autoStartBoss;
    }
    

    /**
     * @return Boss if connected or null otherwise.
     */
    public synchronized Boss getBoss() {
        return boss;
    }


    public boolean isVerbose() {
        return verbose;
    }


    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    public void setTimerTimeout(long timerTimeout) {
        this.timerTimeout = timerTimeout;
    }

}
