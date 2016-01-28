package io.digdag.cli.client;

import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import com.beust.jcommander.Parameter;
import io.digdag.cli.Main;
import io.digdag.cli.Command;
import io.digdag.cli.SystemExitException;
import io.digdag.client.DigdagClient;
import io.digdag.cli.YamlMapper;
import static io.digdag.cli.Main.systemExit;

public abstract class ClientCommand
    extends Command
{
    @Parameter(names = {"-e", "--endpoint"})
    protected String endpoint = "127.0.0.1:65432";

    @Override
    public void main()
        throws Exception
    {
        try {
            mainWithClientException();
        }
        catch (ClientErrorException ex) {
            Response res = ex.getResponse();
            switch (res.getStatus()) {
            case 404:  // NOT_FOUND
                throw systemExit("Resource not found: " + res.readEntity(String.class));
            case 409:  // CONFLICT
                throw systemExit("Request conflicted: " + res.readEntity(String.class));
            case 422:  // UNPROCESSABLE_ENTITY
                throw systemExit("Invalid option: " + res.readEntity(String.class));
            default:
                throw systemExit("Status code " + res.getStatus() + ": " + res.readEntity(String.class));
            }
        }
    }

    public abstract void mainWithClientException()
        throws Exception;

    protected DigdagClient buildClient()
    {
        String[] fragments = endpoint.split(":", 2);
        String host;
        int port;
        if (fragments.length == 1) {
            host = fragments[0];
            port = 80;
        }
        else {
            host = fragments[0];
            port = Integer.parseInt(fragments[1]);
        }

        return DigdagClient.builder()
            .host(host)
            .port(port)
            .build();
    }

    public static void showCommonOptions()
    {
        System.err.println("    -e, --endpoint HOST[:PORT]       HTTP endpoint (default: 127.0.0.1:65432)");
        Main.showCommonOptions();
    }

    public long parseLongOrUsage(String arg)
        throws SystemExitException
    {
        try {
            return Long.parseLong(args.get(0));
        }
        catch (NumberFormatException ex) {
            throw usage(ex.getMessage());
        }
    }

    protected ModelPrinter modelPrinter()
    {
        return new ModelPrinter();
    }

    protected static void ln(String format, Object... args)
    {
        System.out.println(String.format(format, args));
    }

    private final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault());

    protected String formatTime(long unix)
    {
        return formatTime(Instant.ofEpochSecond(unix));
    }

    protected String formatTime(Instant instant)
    {
        return formatter.format(instant);
    }

    protected static String formatTimeDiff(Instant now, long from)
    {
        long seconds = now.until(Instant.ofEpochSecond(from), ChronoUnit.SECONDS);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        if (hours > 0) {
            return String.format("%2dh %2dm %2ds", hours, minutes, seconds);
        }
        else if (minutes > 0) {
            return String.format("    %2dm %2ds", minutes, seconds);
        }
        else {
            return String.format("        %2ds", seconds);
        }
    }

    private final DateTimeFormatter parser =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault());

    protected Instant parseTime(String s)
        throws DateTimeParseException
    {
        try {
            Instant i = Instant.ofEpochSecond(Long.parseLong(s));
            System.err.println("Using unix timestamp " + i);
            return i;
        }
        catch (NumberFormatException ex) {
            return Instant.from(parser.parse(s));
        }
    }

    protected static YamlMapper yamlMapper()
    {
        return new YamlMapper(ModelPrinter.objectMapper());
    }
}