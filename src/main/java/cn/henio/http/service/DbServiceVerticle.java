package cn.henio.http.service;

import cn.henio.AutoRegisterVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/26 15:24].
 */
@Component
public class DbServiceVerticle extends AutoRegisterVerticle{
  @Autowired
  private Vertx vertx;
  @Autowired
  private JDBCClient dbClient;

  public static final String CONFIG_WIKIDB_JDBC_URL = "wikidb.jdbc.url";
  public static final String CONFIG_WIKIDB_JDBC_DRIVER_CLASS = "wikidb.jdbc.driver_class";
  public static final String CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size";
  public static final String CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE = "wikidb.sqlqueries.resource.file";

  public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

  private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceVerticle.class);

  // (...)
  // end::preamble[]

  // tag::loadSqlQueries[]
  public enum SqlQuery {
    CREATE_PAGES_TABLE,
    ALL_PAGES,
    GET_PAGE,
    CREATE_PAGE,
    SAVE_PAGE,
    DELETE_PAGE,
    ALL_PAGES_DATA,
    GET_PAGE_BY_ID
  }

  public static final HashMap<SqlQuery, String> sqlQueries = new HashMap<>();

  /*
    * Note: this uses blocking APIs, but data is small...
    */
  // <1>
  static {
    String queriesFile = /*config().getString(CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE)*/ null;
    InputStream queriesInputStream = null;
    if (queriesFile != null) {
      try {
        queriesInputStream = new FileInputStream(queriesFile);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      queriesInputStream = DbServiceVerticle.class.getResourceAsStream("/db-queries.properties");
    }

    Properties queriesProps = new Properties();
    try {
      queriesProps.load(queriesInputStream);
      queriesInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    sqlQueries.put(SqlQuery.CREATE_PAGES_TABLE, queriesProps.getProperty("create-pages-table"));
    sqlQueries.put(SqlQuery.ALL_PAGES, queriesProps.getProperty("all-pages"));
    sqlQueries.put(SqlQuery.GET_PAGE, queriesProps.getProperty("get-page"));
    sqlQueries.put(SqlQuery.CREATE_PAGE, queriesProps.getProperty("create-page"));
    sqlQueries.put(SqlQuery.SAVE_PAGE, queriesProps.getProperty("save-page"));
    sqlQueries.put(SqlQuery.DELETE_PAGE, queriesProps.getProperty("delete-page"));
    sqlQueries.put(SqlQuery.ALL_PAGES_DATA, queriesProps.getProperty("all-pages-data"));
    sqlQueries.put(SqlQuery.GET_PAGE_BY_ID, queriesProps.getProperty("get-page-by-id"));
  }
  // end::loadSqlQueries[]

  // tag::start[]
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer(config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue"), this::onMessage);  // <3>
  }
  // end::start[]

  // tag::onMessage[]
  public enum ErrorCodes {
    NO_ACTION_SPECIFIED,
    BAD_ACTION,
    DB_ERROR
  }

  public void onMessage(Message<JsonObject> message) {

    if (!message.headers().contains("action")) {
      LOGGER.error("No action header specified for message with headers {} and body {}",
          message.headers(), message.body().encodePrettily());
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
      return;
    }
    String action = message.headers().get("action");

    switch (action) {
      case "all-pages":
        fetchAllPages(message);
        break;
      case "get-page":
        fetchPage(message);
        break;
      case "create-page":
        createPage(message);
        break;
      case "save-page":
        savePage(message);
        break;
      case "delete-page":
        deletePage(message);
        break;
      case "all-pages-data":
        fetchAllPagesData(message);
        break;
      case "get-page-by-id":
        fetchPageById(message);
        break;
      default:
        message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
    }
  }
  // end::onMessage[]

  private void _fetchAllPages(Message<JsonObject> message) {
    // tag::query-with-connection[]
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(sqlQueries.get(SqlQuery.ALL_PAGES), res -> {
          connection.close();
          if (res.succeeded()) {
            List<String> pages = res.result()
                .getResults()
                .stream()
                .map(json -> json.getString(0))
                .sorted()
                .collect(Collectors.toList());
            message.reply(new JsonObject().put("pages", new JsonArray(pages)));
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
    // end::query-with-connection[]

    // tag::query-simple-oneshot[]  3.5.2 才可用
   /* dbClient.query(sqlQueries.get(SqlQuery.ALL_PAGES), res -> {
      if (res.succeeded()) {
        List<String> pages = res.result()
            .getResults()
            .stream()
            .map(json -> json.getString(0))
            .sorted()
            .collect(Collectors.toList());
        message.reply(new JsonObject().put("pages", new JsonArray(pages)));
      } else {
        reportQueryError(message, res.cause());
      }
    });*/
    // end::query-simple-oneshot[]
  }

  // tag::rest[]
  private void fetchAllPages(Message<JsonObject> message) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(sqlQueries.get(SqlQuery.ALL_PAGES), res -> {
          connection.close();
          if (res.succeeded()) {
            List<String> pages = res.result()
                .getResults()
                .stream()
                .map(json -> json.getString(0))
                .sorted()
                .collect(Collectors.toList());
            message.reply(new JsonObject().put("pages", new JsonArray(pages)));
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void fetchAllPagesData(Message<JsonObject> message) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(sqlQueries.get(SqlQuery.ALL_PAGES_DATA), res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply(res.result().toJson());
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void fetchPage(Message<JsonObject> message) {
    String requestedPage = message.body().getString("page");
    JsonArray params = new JsonArray().add(requestedPage);

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.queryWithParams(sqlQueries.get(SqlQuery.GET_PAGE), params, fetch -> {
          connection.close();
          if (fetch.succeeded()) {
            JsonObject response = new JsonObject();
            ResultSet resultSet = fetch.result();
            if (resultSet.getNumRows() == 0) {
              response.put("found", false);
            } else {
              response.put("found", true);
              JsonArray row = resultSet.getResults().get(0);
              response.put("id", row.getInteger(0));
              response.put("rawContent", row.getString(1));
            }
            message.reply(response);
          } else {
            reportQueryError(message, fetch.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  public void fetchPageById(Message<JsonObject> message) {
    Integer requestedPage = message.body().getInteger("id");
    JsonArray params = new JsonArray().add(requestedPage);

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.queryWithParams(sqlQueries.get(SqlQuery.GET_PAGE_BY_ID), params, fetch -> {
          connection.close();
          if (fetch.succeeded()) {
            JsonObject response = new JsonObject();
            ResultSet resultSet = fetch.result();
            if (resultSet.getNumRows() == 0) {
              response.put("found", false);
            } else {
              response.put("found", true);
              JsonObject row = resultSet.getRows().get(0);
              response.put("id", row.getInteger("ID"));
              response.put("name", row.getString("NAME"));
              response.put("content", row.getString("CONTENT"));
            }
            message.reply(response);
          } else {
            reportQueryError(message, fetch.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void createPage(Message<JsonObject> message) {
    JsonObject request = message.body();
    JsonArray data = new JsonArray()
        .add(request.getString("title"))
        .add(request.getString("markdown"));

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.updateWithParams(sqlQueries.get(SqlQuery.CREATE_PAGE), data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void savePage(Message<JsonObject> message) {
    JsonObject request = message.body();
    JsonArray data = new JsonArray()
        .add(request.getString("markdown"))
        .add(request.getString("id"));

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.updateWithParams(sqlQueries.get(SqlQuery.SAVE_PAGE), data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void deletePage(Message<JsonObject> message) {
    JsonArray data = new JsonArray().add(message.body().getString("id"));

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.updateWithParams(sqlQueries.get(SqlQuery.DELETE_PAGE), data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void reportQueryError(Message<JsonObject> message, Throwable cause) {
    LOGGER.error("Database query error", cause);
    message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
  }
  // end::rest[]
}
