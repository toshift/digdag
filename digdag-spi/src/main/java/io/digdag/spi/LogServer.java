package io.digdag.spi;

public interface LogServer
{
    void send(long taskId, byte[] data);

    byte[] get(long taskId, int index);
}
