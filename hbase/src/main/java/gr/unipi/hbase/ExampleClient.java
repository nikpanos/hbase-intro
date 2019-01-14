package gr.unipi.hbase;
import java.io.IOException;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

public class ExampleClient {

  public static void main(String[] args) throws IOException, ServiceException {
    Configuration config = HBaseConfiguration.create();
    // Create table
    HBaseAdmin.checkHBaseAvailable(config);
    Connection connection = ConnectionFactory.createConnection(config);
    Admin admin = connection.getAdmin();
    try {
      TableName tableName = TableName.valueOf("test");
      HTableDescriptor desc = new HTableDescriptor(tableName);
      desc.addFamily(new HColumnDescriptor("data"));
      admin.createTable(desc);
      HTableDescriptor[] tables = admin.listTables();
      if (tables.length != 1 &&
          Bytes.equals(tableName.getName(), tables[0].getTableName().getName())) {
        throw new IOException("Failed to create table");
      }
      // Run some operations -- three puts, a get, and a scan -- against the table.
      Table table = connection.getTable(tableName);
      try {
        for (int i = 1; i <= 3; i++) {
          byte[] row = Bytes.toBytes("row" + i);
          Put put = new Put(row);
          byte[] columnFamily = Bytes.toBytes("data");
          byte[] qualifier = Bytes.toBytes(String.valueOf(i));
          byte[] value = Bytes.toBytes("value" + i);
          put.addColumn(columnFamily, qualifier, value);
          table.put(put);
        }
        Get get = new Get(Bytes.toBytes("row1"));
        Result result = table.get(get);
        System.out.println("Get: " + result);
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        try {
          for (Result scannerResult : scanner) {
            System.out.println("Scan: " + scannerResult);
          }
        } finally {
          scanner.close();
        }
        // Disable then drop the table
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
      } finally {
        table.close();
      }
    } finally {
      admin.close();
    }
  }
}