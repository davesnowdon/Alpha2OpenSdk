package com.ubtech.alpha2.core.network.http;

import android.os.SystemClock;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import javax.net.ssl.SSLException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

class RetryHandler implements HttpRequestRetryHandler {
   private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
   private static HashSet<Class<?>> exceptionWhitelist = new HashSet();
   private static HashSet<Class<?>> exceptionBlacklist = new HashSet();
   private final int maxRetries;

   public RetryHandler(int maxRetries) {
      this.maxRetries = maxRetries;
   }

   public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
      boolean retry = true;
      Boolean b = (Boolean)context.getAttribute("http.request_sent");
      boolean sent = b != null && b;
      if (executionCount > this.maxRetries) {
         retry = false;
      } else if (this.isInList(exceptionBlacklist, exception)) {
         retry = false;
      } else if (this.isInList(exceptionWhitelist, exception)) {
         retry = true;
      } else if (!sent) {
         retry = true;
      }

      if (retry) {
         HttpUriRequest currentReq = (HttpUriRequest)context.getAttribute("http.request");
         String requestType = currentReq.getMethod();
         retry = !requestType.equals("POST");
      }

      if (retry) {
         SystemClock.sleep(1500L);
      } else {
         exception.printStackTrace();
      }

      return retry;
   }

   protected boolean isInList(HashSet<Class<?>> list, Throwable error) {
      Iterator itr = list.iterator();

      do {
         if (!itr.hasNext()) {
            return false;
         }
      } while(!((Class)itr.next()).isInstance(error));

      return true;
   }

   static {
      exceptionWhitelist.add(NoHttpResponseException.class);
      exceptionWhitelist.add(UnknownHostException.class);
      exceptionWhitelist.add(SocketException.class);
      exceptionBlacklist.add(InterruptedIOException.class);
      exceptionBlacklist.add(SSLException.class);
   }
}
