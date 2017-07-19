# 设计模式之适配器模式
　　这两天做了个小工具，其中需要对Excel文件进行读写，用的工具是`apache poi`。Excel文件常用的有两种格式，`.xls`和`.xlsx`(还有`.xlsb`、`xlsm`和`xlst`，详情请查看[这篇文章](http://mtoou.info/excel-lijie/))。如果要对不同后缀的Excel文件进行读写，要用到不同的类，但是里边的代码几乎一样，为了可扩展性，就想到了用一种设计模式，脑海中有个大概的结构，但是不知道叫什么，就先写代码，写完之后再看，感觉像是适配器模式。
## `ExcelFile`接口
　　首先创建一个`ExcelFile`接口，这个接口中有一个方法`readContentStr`，用于读取Excel中的内容，返回字符串。
``` Java
public interface ExcelFile {
    String readContentStr() throws IOException;
}
```
## `ExcelFile`接口实现类
　　编写`XLSFile`类和`XLSXFile`类，继承`ExcelFile`接口，内部实现根据文件类型选择不同的`poi`类。
### `XLSFile`类
``` Java
@Override
public class XLSFile implements ExcelFile {

    @Override
    public String readContentStr() throws IOException {
        // do something
    }

}
```
### `XLSXFile`类
``` Java
public class XLSXFile implements ExcelFile {

    @Override
    public String readContentStr() throws IOException {
        // do something
    }

}
```
## `ExcelReader`
　　`ExcelReader`是调用`ExcelFile`的类
``` Java
public class ExcelReader {

    private ExcelFile excel;

    public ExcelReader(File file) throws IOException {
        // 初始化excel，根据文件类型创建不同的ExcelFile实现类对象
    }

    public String parseContentString() throws IOException {
        return excel.readContentStr();
    }

}
```
## 到底是什么模式
　　`ExcelReader`内部调用了`Excel`接口的方法，可以说是`Excel`的代理;`ExcelReader`内部又对传进来的文件进行类型判断，进而创建不同的`Excel`对象，这是为了适配不同的文件类型，也可以说是适配器。所以不伦不类？
  
　　查了一下它们的区别，看了好大会也看不懂，决定放下，等以后代码量上来，或者有足够复杂的业务时再好好整理。
## 进行改进
　　`XLSFile`和`XLSXFile`中的`readContentStr`方法，内部创建对象时使用的是不同的`poi`对象`HSSFWorkbook`和`XSSFWorkbook`，其余代码都相同，而且`HSSFWorkbook`和`XSSFWorkbook`都实现了相同的接口`Workbook`，因此可以将相同的代码抽取出一个方法，参数是传入`Workbook`，实现代码复用。如果这样做的话需要把`ExcelFile`接口改为抽象类。
