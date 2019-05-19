package com.ducnguyen.synchronization.wifip2p.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by pv on 21/06/17.
 */

class WifiP2pConnectImpl implements WifiP2pConnect {
    private static final int SERVER_STARTED = 1;
    private static final int SERVER_STOPPED = 2;
    private final Context mContext;
    private final Listener mListener;
    private final Looper mListenerLooper;
    private final Set<Host> mPeers;
    private int mServerState;
    private List<byte[]> mSendDataQueue = new ArrayList<>();
    private List<Host> mSendDestQueue = new ArrayList<>();
    private List<Long> mSendJobQueue = new ArrayList<>();
    private TcpClientService.Listener mClientServiceListener;
    private TcpServerService.TcpServerListener mServerServiceListener;

    WifiP2pConnectImpl(Context context, Listener listener, Looper looper, Set<Host> peers) {
        mContext = context;
        mListener = listener;
        mListenerLooper = looper;
        mPeers = peers;
    }

    private ServiceConnection mClientConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TcpClientService.TcpClientBinder) {
                TcpClientService.TcpClientBinder binder = (TcpClientService.TcpClientBinder) service;
                binder.setListener(getClientServiceListener(), Looper.myLooper());
                byte[] candidateData = null;
                Host candidateHost = null;
                long jobId = 0;
                while (mSendDataQueue.size() > 0) {
                    synchronized (WifiP2pConnectImpl.this) {
                        if (mSendDataQueue.size() > 0) {
                            candidateData = mSendDataQueue.remove(0);
                            candidateHost = mSendDestQueue.remove(0);
                            jobId = mSendJobQueue.remove(0);
                        }
                    }
                    binder.send(candidateData, candidateHost, jobId);
                }
                mContext.unbindService(this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @NonNull
    private TcpClientService.Listener getClientServiceListener() {
        if (mClientServiceListener == null) {
            mClientServiceListener = new TcpClientService.Listener() {
                @Override
                public void onSendSuccess(final long jobId) {
                    if (mListenerLooper != null && mListener != null) {
                        new Handler(mListenerLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onSendComplete(jobId);
                            }
                        });
                    }
                }

                @Override
                public void onSendFailure(final long jobId, final Throwable e) {
                    if (mListenerLooper != null && mListener != null) {
                        new Handler(mListenerLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onSendFailure(e, jobId);
                            }
                        });
                    }
                }
            };
        }
        return mClientServiceListener;
    }

    @Override
    public long send(byte[] bytes, Host host) {
        long jobId = System.currentTimeMillis();
        synchronized (WifiP2pConnectImpl.this) {
            mSendDataQueue.add(bytes);
            mSendDestQueue.add(host);
            mSendJobQueue.add(jobId);
        }

        Intent intent = new Intent(mContext.getApplicationContext(), TcpClientService.class);
        mContext.startService(intent);

        mContext.bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);

        return jobId;
    }

    private ServiceConnection mStartServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TcpServerService.TcpServerBinder && mServerState == SERVER_STARTED) {
                TcpServerService.TcpServerBinder binder = (TcpServerService.TcpServerBinder) service;
                binder.setListener(getServerServiceListener());
                binder.startServer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @NonNull
    private TcpServerService.TcpServerListener getServerServiceListener() {
        if (mServerServiceListener == null) {
            mServerServiceListener = new TcpServerService.TcpServerListener() {

                @Override
                public void onServerStartFailed(final Throwable e) {
                    if (mListener != null && mListenerLooper != null) {
                        new Handler(mListenerLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onStartListenFailure(e);
                            }
                        });
                    }
                }

                @Override
                void onReceive(final byte[] bytes, final InetAddress inetAddress) {
                    if (mListener != null && mListenerLooper != null) {
                        new Handler(mListenerLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                for (Host peer : mPeers) {
                                    if (peer.getHostAddress().equals(inetAddress.getHostAddress())) {
                                        mListener.onReceive(bytes, peer);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
            };
        }
        return mServerServiceListener;
    }

    private ServiceConnection mStopServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TcpServerService.TcpServerBinder && mServerState == SERVER_STOPPED) {
                TcpServerService.TcpServerBinder binder = (TcpServerService.TcpServerBinder) service;
                binder.stopServer();
                mContext.unbindService(this);
                mContext.unbindService(mStartServerConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void startReceiving() {
        if (mServerState != SERVER_STARTED) {
            mServerState = SERVER_STARTED;
            Intent intent = new Intent(mContext.getApplicationContext(), TcpServerService.class);
            mContext.startService(intent);

            mContext.bindService(intent, mStartServerConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void stopReceiving(boolean abortCurrentTransfers) {
//        TODO: handle abort param
        if (mServerState != SERVER_STOPPED) {
            mServerState = SERVER_STOPPED;
            Intent intent = new Intent(mContext.getApplicationContext(), TcpServerService.class);
            mContext.startService(intent);

            mContext.bindService(intent, mStopServerConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Set<Host> getPeers() {
        return mPeers;
    }

    @Override
    public boolean isReceiving() {
        return mServerState == SERVER_STARTED;
    }
}
