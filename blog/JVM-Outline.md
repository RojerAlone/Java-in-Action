# 对象是怎么降生的
## 从`.java`文件到`.class`文件
　　计算机硬件只能识别机器码0和1，Java编译器（`javac`）将`.java`文件编译成`.class`的字节码文件，再由JVM加载`.class`文件，执行时解释为机器码进行执行，可以说Java是一种半编译半解释型的语言。
## JVM加载`. class`文件
### `.class`文件的结构
> Class文件是一组以8位字节为基础单位的二进制流，各个数据项目严格按照顺序紧凑地排列在Class 文件之中，中间没有添加任何分隔符，这使得整个Class文件中存储的内容几乎全部是程序运行的必要数据，没有空隙存在。 当遇到需要占用8位字节以上空间的数据项时，则会按照高位在前的方式分割成若干个8位字节进行存储。<div align="right">——《深入理解Java虚拟机》</div>
### 加载`.class`文件
　　虚拟机把描述类的数据从Class文件加载到内存，并对数据进行校验、 转换解析和初始化，最终形成可以被虚拟机直接使用的Java类型，这就是虚拟机的类加载机制。
### 类的生命周期
　　类从被加载到虚拟机内存一直到卸载出内存，经历了以下过程：
　　![类加载过程](http://img.blog.csdn.net/20170711143829163?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　其中验证、准备、解析三个阶段统称为连接，解析阶段在某些情况下可以在初始化阶段之后再开始，这是为了支持Java的运行时绑定。
#### 加载
　　加载阶段完成3件事情：

　　 1.  通过一个类的全限定名来获取定义此类的二进制字节流（可能是`.class`文件，也可能从JAR包获取或者从网络获取）

　　 2.  将这个字节流锁代表的静态数据结构转化为方法区的运行时数据结构

　　 3.  在内存中生成一个代表这个类的`java.lang.Class`对象，作为方法区这个类的各种数据的访问入口

　　 加载阶段可以使用系统提供的类加载器（`ClassLoader`）来完成，也可以由用户自定义的类加载器完成，开发人员可以通过定义类加载器去控制字节流的获取方式。
#### 验证
　　确保class文件字节流的正确性，符合虚拟机的要求，并且不会危害虚拟机自身的安全，要验证比如：

- 文件格式（是否以魔数0xCAFEBABE开头、版本号是否在当前虚拟机可处理的版本范围内等）
- 元数据验证（是否有父类、是否继承了final修饰的类、是否实现了其父类或者接口中要求实现的方法、是否和父类产生矛盾），也就是是否符合Java的语法
- 字节码验证（通过数据流和控制流分析，确定程序语义是合法的、 符合逻辑的）
- 符号引用验证，发生在虚拟机将符号引用转化为直接引用的时候（发生在解析阶段），主要是对类自身以外的信息（常量池中的各种符号引用）进行匹配性的校验
#### 准备
　　准备阶段是正式为类变量分配内存并设置类变量初始值的阶段，这里所设置的初始值通常情况下是数据类型默认的零值（如 0、0L、null、false 等），而不是被在 Java 代码中被显式地赋予的值，赋值发生在初始化阶段。
#### 解析
　　解析阶段是虚拟机将常量池内的符号引用替换为直接引用的过程，符号引用是用一组符号来描述引用的目标，只要能定位到目标即可，直接引用可以是直接指向目标的指针、相对偏移量或者能间接定位到目标的句柄，简而言之就是能定位目标的一个信息。
#### 初始化
　　初始化阶段才真正执行用户定义的程序代码，执行类的构造方法，包括赋值类变量、静态语句块的合并（static{}块的语句合并）。JVM保证了初始化的线程安全，如果多个线程同时去初始化一个类，那么只有一个线程会执行初始化。
## 类加载器
　　虚拟机设计团队把类加载阶段中的“通过一个类的全限定名来获取描述此类的二进制字节流”这个动作放到Java虚拟机外部去实现，以便让应用程序自己决定如何去获取所需要的类。 实现这个动作的代码模块称为“类加载器”。

　　从Java虚拟机的角度来讲，只存在两种不同的类加载器：一种是启动类加载器（Bootstrap ClassLoader），这个类加载器使用C++语言实现，是虚拟机自身的一部分；另一种就是所有其他的类加载器，这些类加载器都由Java语言实现，独立于虚拟机外部，并且全都继承自抽象类`java.lang.ClassLoader`。
# 对象在哪里生存
　　Java生存在JVM（Java Virtual Machine，Java虚拟机）中。相对于C、C++需要程序员自己进行内存的管理，Java程序员显得不那么操心，内存管理交给了JVM来进行管理，只需要关注于业务的实现，但是因为“放弃”了内存的控制权，当出现了内存泄漏或溢出问题需要程序员自己去排查问题时，就需要开发人员对JVM的内存有足够的了解才能解决问题。
## JVM的内存区域
　　JVM将它所管理的内存区域分为了几个部分，每个部分有各自的用途以及创建、销毁时间，内存划分如下图所示：
　　![JVM_Memory](http://upload-images.jianshu.io/upload_images/5401760-cde4aefdad5438ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
### 程序计数器
　　程序计数器是一块较小的内存区域，是当前线程执行的字节码的行号记录器，字节码解释器工作的时候从这个程序计数器中取下一行要执行的指令。多线程切换时CPU要记录当前线程的状态信息，为了以后切换回这个线程时知道要执行的指令位置，这时就要用到了程序计数器，同时程序计数器是线程私有的。

　　如果线程正在执行一个Native方法，程序计数器的值为空（Undefined）。
### 虚拟机栈
　　虚拟机栈是线程私有的，它的生命周期和程序计数器相同。

　　虚拟机栈中存储的是一个个的“栈帧”，每个栈帧的入栈和出栈代表一个方法的调用和执行完成，栈帧中存储局部变量表、操作数栈、动态链接、方法出口等信息。

![虚拟机栈](http://img.blog.csdn.net/20170711093303823?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　当虚拟机栈的栈深度大于虚拟机允许的深度，将抛出`StackOverflowError`异常，当虚拟机栈允许动态扩展时，如果扩展时无法申请到足够的内存，就会抛出`OutOfMemoryError`异常。
#### 局部变量表
　　存放方法参数和方法内部定义的局部变量。
#### 操作数栈
　　在方法的执行过程中，会有各种字节码指令往操作数栈中写入和提取内容，在做算术运算的时根据运算符的类型取出栈顶的若干个元素进行操作。
#### 动态链接
　　在Class文件的常量池中存有大量的符号引用，字节码中的方法调用指令就以常量池中指向方法的符号引用为参数。这些符号引用一部分会在类加载阶段或第一次使用的时候转化为直接引用，这种转化称为静态解析。另外一部分将在每一次的运行期期间转化为直接引用，这部分称为动态连接。
#### 方法出口
　　一个方法的退出有两种情况：正常退出和异常退出。正常退出是指方法正常返回或正常顺序执行完毕，可能会返回给调用者信息，方法出口存放的信息帮助调用者恢复执行状态。异常退出指方法中发生了未处理的异常导致方法退出，交给专门的异常处理器处理。
### 本地方法栈
　　和虚拟机栈类似，只不过存放的是Native方法，因为虚拟机规范对本地方法栈中使用的语言、方式和数据结构没有强制要求，有些虚拟机直接把本地方法栈和虚拟机栈合二为一，比如Sum HotSpot。

　　本地方法栈也会抛出`StackOverflowError`和`OutOfMemoryError`异常。
### 堆
　　Java程序员常说“堆栈”，栈就是刚才所说的虚拟机栈，堆就是这里要说的Java堆（`Java Heap`），它是Java虚拟机所管理的内存中最大的一块，因为它只存放对象实例，几乎所有的对象实例都存放在这里（JIT编译的对象可能分配在栈上），同时它是被所有线程共享的。

　　Java堆是垃圾收集器主要管理的区域，因此也叫作GC堆。

　　当堆内存无法分配给对象足够的内存，并且也无法扩展内存时，会抛出`OutOfMemoryError`异常。
### 方法区
　　方法区也是线程共享的区域，用于存储已经被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据，可以理解为类的描述信息。方法区也受到GC的管理，也会抛出`OutOfMemoryError`异常。
### 运行时常量池
　　运行时常量池是方法区的一部分（JDK1.7已经把常量池转移到堆里面了），Class文件中除了有类的版本、字段、方法、接口等描述信息以外，还有一项信息是常量池，用于存放编译期生成的各种字面量和符号引用，这部分内容将在类加载后放到运行时常量池中。

　　运行时常量池相对于CLass文件常量池的另外一个重要特征是具备动态性，运行期间也可能将新的常量放入池中，这种特性被开发人员利用比较多的就是String类的`intern()`方法（JDK1.7之后，如果字符串对象已经存在，那么常量池中存放的是该字符串的引用）。
# 对象的朝生夕灭
　　在Java中“万物皆对象”，无时无刻都有对象被创建以及销毁。内存空间是有限的，当内存空间不够的时候，应该回收已经“死去”的对象占用的空间，那么如何判断对象“死去”了以及如何回收？
## 引用计数算法
　　给对象添加一个引用计数器，每当有一个地方引用这个对象，计数器值加1，引用失效时计数器值减1，当计数器值为0时，这个对象就“死去”了，这样的算法简单高效，大多数情况下都是有效的，但是当出现循环引用时就无法回收对象，如下所示：
``` Java
public class Obj {

	public Obj instance;
	
	public static void main(String[] args) {
		Obj obj1 = new Obj();
		Obj obj2 = new Obj();
		obj1.instance = obj2;
		obj2.instance = obj1;
	}

}
```
　　`obj1`和`obj2`相互引用，这样计数器值都为1，虽然这两个对象已经不再使用了，但是却无法被回收。
## 可达性分析算法
　　这个算法的思想是，通过一系列的被称为“GC Roots”的对象作为起点，从这些节点开始向下搜索，搜索所走过的路径称为引用链（Reference Chain），当一个对象到GC Roots没有任何引用链相连（用图论的话来说，就是从GC Roots到这个对象不可达）时，则证明此对象是不可用的。

　　可作为GC Roots的对象包括以下几种：

 - 虚拟机栈（栈帧中的本地变量表）中引用的对象
 - 方法区中类静态属性引用的对象
 - 方法区中常量引用的对象
 - 本地方法栈中JNI（即一般说的Native方法）引用的对象
## 引用
　　无论是引用计数算法还是可达性分析算法，判断对象存货与否都与“引用”有关，Java中的引用分为强引用、软引用、弱引用和虚引用4中，引用强度依次减弱。
### 强引用
　　强引用指在程序代码中普遍存在的，类似`Object obj=new Object（）`这类的引用，只要强引用还存在，垃圾收集器永远不会回收掉被引用的对象，哪怕抛出`OutOfMemoryError`异常也不会回收这个对象。
### 软引用
　　软引用是用来描述一些还有用但并非必需的对象。 对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围之中进行第二次回收。 如果这次回收还没有足够的内存，才会抛出内存溢出异常。 在JDK 1.2之后，提供了`SoftReference`类来实现软引用。

　　如果一个对象只具有软引用，那就类似于可有可物的生活用品。如果内存空间足够，垃圾回收器就不会回收它，如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。
### 弱引用
　　弱引用也是用来描述非必需对象的，但是它的强度比软引用更弱一些，被弱引用关联的对象只能生存到下一次垃圾收集发生之前。 当垃圾收集器工作时，无论当前内存是否足够，都会回收掉只被弱引用关联的对象。 在JDK 1.2之后，提供了`WeakReference`类来实现弱引用。

　　如果一个对象只具有弱引用，那就类似于可有可物的生活用品。弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程， 因此不一定会很快发现那些只具有弱引用的对象。
### 虚引用
　　虚引用也称为幽灵引用或者幻影引用，它是最弱的一种引用关系。 一个对象是否有虚引用的存在，完全不会对其生存时间构成影响，也无法通过虚引用来取得一个对象实例。 为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知。 在JDK 1.2之后，提供了`PhantomReference`类来实现虚引用。

　　虚引用与软引用和弱引用的一个区别在于：虚引用必须和引用队列（`ReferenceQueue`）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之关联的引用队列中。程序可以通过判断引用队列中是否已经加入了虚引用，来了解被引用的对象是否将要被垃圾回收。程序如果发现某个虚引用已经被加入到引用队列，那么就可以在所引用的对象的内存被回收之前采取必要的行动。
## GC算法
　　GC（Garbage Collection），是指虚拟机对内存中无用的对象空间进行的回收，下面是一些常用的GC算法。
### 标记-清除算法
　　标记-清除算法分为“标记”和“清除”两个阶段：首先标记出所有需要回收的对象，在标记完成后统一回收所有被标记的对象。

　　一些其他的收集算法都是基于这种思路并对其不足进行改进而得到的。 它的主要不足有两个：一个是效率问题，标记和清除两个过程的效率都不高；另一个是空间问题，标记清除之后会产生大量不连续的内存碎片，空间碎片太多可能会导致以后在程序运行过程中需要分配较大对象时，无法找到足够的连续内存而不得不提前触发另一次垃圾收集动作。 
　　![标记清除算法](http://img.blog.csdn.net/20170711203227999?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
### 复制算法
　　为了解决效率问题，一种称为“复制”（Copying）的收集算法出现了，它将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。 当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉。 这样使得每次都是对整个半区进行内存回收，内存分配时也就不用考虑内存碎片等复杂情况，只要移动堆顶指针，按顺序分配内存即可，实现简单，运行高效。 只是这种算法的代价是将内存缩小为了原来的一半，未免太高了一点。 
　　![复制算法](http://img.blog.csdn.net/20170711203605418?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　复制算法用来回收新生代，将新生代内存分为较大的Eden（伊甸园）空间和两块较小的Survivor（幸存者）空间，每次使用Eden和其中一块Survivor空间，回收时将Eden和Survivor中还存活着的对象一次性地复制到另外一块Survivor空间上，最后清理掉Eden和刚才用过的Survivor空间。

　　HotSpot虚拟机默认Eden和Survivor的大小比例是8:1，也就是每次新生代中可用内存空间为整个新生代容量的90%（80%+10%），只有10%的内存会被“浪费”。我们没有办法保证每次回收都只有不多于10%的对象存活，当Survivor空间不够用时，需要依赖其他内存（这里指老年代）进行分配担保（Handle Promotion）。
### 标记-整理算法
　　复制收集算法在对象存活率较高时就要进行较多的复制操作，效率将会变低。 更关键的是，如果不想浪费50%的空间，就需要有额外的空间进行分配担保，以应对被使用的内存中所有对象都100%存活的极端情况，所以在老年代一般不能直接选用这种算法。

　　标记-整理算法的标记过程仍然与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，然后直接清理掉端边界以外的内存。
　　![标记整理算法](http://img.blog.csdn.net/20170711204448713?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
### 分代收集算法
　　分代收集算法根据对象存活周期的不同将内存划分为几块。 一般是把Java堆分为新生代和老年代（JDK1.8之后永久代被移除，取而代之的是一个叫元空间 MetaSpace 的区域），这样就可以根据各个年代的特点采用最适当的收集算法。 在新生代中，每次垃圾收集时都发现有大批对象死去，只有少量存活，那就选用复制算法，只需要付出少量存活对象的复制成本就可以完成收集。 而老年代中因为对象存活率高、 没有额外空间对它进行分配担保，就必须使用“标记—清理”或者“标记—整理”算法来进行回收。
## 内存分配策略
　　Java中的内存管理可归结为两个方面：给对象分配内存和回收内存。回收内存就是GC，内存分配有几个普遍的规则。
### 堆内存大小参数
　　VM的选项有3种：

 - \- : 标准VM选项，VM规范的选项
 - -X: 非标准VM选项，不保证所有VM支持
 - -XX: 高级选项，高级特性，但属于不稳定的选项
 
|参数|含义|
|:----|:----|
| -Xms | Memory Startup，初始堆大小 |
| -Xmx | Memory Maximum，堆的最大值 |
| -Xmn | Memory New/Nursery，新生代初始及最大大小，如果需要进一步细化，初始化大小用-XX:NewSize，最大大小用-XX:MaxNewSize |
| -Xss | Stack Size，线程栈大小，等同于-XX:ThreadStackSize |
### 对象优先在Eden分配
　　大多数情况下对象在新生代Eden区中分配，当Eden区中没有足够的内存分配时，虚拟机将进行一次Minor GC。
#### 新生代GC（Minor GC）
　　发生在新生代的垃圾收集动作，因为Java对象大多都具备朝生夕灭的特性，所以Minor GC非常频繁，一般回收速度也比较快。
#### 老年代GC（Major GC/Full GC）
　　发生在老年代的GC，出现了Major GC，经常会伴随至少一次的Minor GC（但非绝对的，在有的收集器的收集策略里就有直接进行Major GC的策略选择过程）。 Major GC的速度一般会比Minor GC慢10倍以上。
### 大对象直接进入老年代
　　大对象是指需要大量连续内存空间的对象，比如很大的数组。虚拟机提供了一个`-XX：PretenureSizeThreshold`参数，令大于这个设置值的对象直接在老年代分配。 新生代中使用的GC算法是复制算法，大对象直接进入老年代避免在Eden区及两个Survivor区之间发生大量的内存复制。
### 长期存活的对象进入老年代
　　JVM采用的分代收集的思想，新生代老年代，从字面上可以理解为新生的对象、“老去”的对象，也就是存活时间很长的对象，但是对象还没有“死去”。如何判断对象是新生的还是老去的？JVM给每个对象设置了一个年龄计数器，如果对象在Eden区中被创建并经过了一次Minor GC但是仍然存活着，并且能够被Survivor容纳（不用通过担保机制进入老年代），将会被移动到Survivor区中，并且对象年龄为1，就这样每经过一次Minor GC，年龄增加1岁，当它的年龄到了一定的程度（虚拟机默认为15岁，可以通过`-XX：MaxTenuringThreshold`设置）就会被移动到老年代中。
### 动态对象年龄判断
　　为了适应不同程度的内存状况，虚拟机不要求必须到达阈值才移动对象到老年代，如果在Survivor空间中相同年龄所有对象大小的总和大于Survivor空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代。
### 空间分配担保
　　当发生Minor GC之前，虚拟机先检查老年代中最大可用连续空间是否大于新生代所有对象总空间，如果大于那么Minor GC就是安全的，如果空间不够，那么虚拟机会查看虚拟机参数`HandlePromotionFailure`是否设置为了允许担保失败，如果允许就检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于就尝试着进行一次Minor GC，尽管这次Minor GC是有风险的；如果空间不够，或者参数`HandlePromotionFailure`设置为不允许失败，那么会进行一次Full GC。

　　这里检查了历次晋升到老年代对象的平均大小，但是如果本次Minor GC需要的空间很大，那么会分配内存失败，这时候会重新发起一次Full GC清理老年代。如果Full GC后空间还不够，就会抛出`OutOfMemoryError`异常。

　　一般情况下将`HandlePromotionFailure`开关打开（JDK1.6之后默认打开），避免频繁的Full GC。
# 虚拟机性能监控与故障处理工具
　　对Java开发者来说，最常用的命令行工具就是“java.exe”和“javac.exe”这两个，但是JDK的bin目录下有很多工具，这些工具能帮助开发者监控、查看虚拟机状态，对排除故障有很大的帮助。
　　下面是一些常用的命令行工具：

| 名称 | 作用 |
|:-------------:|:-------------|
| jps | JVM Process Status Tool，显示指定系统内所有的HotSpot虚拟机进程 |
| jstat | JVM Statistics Monitoring Tool，用于收集HotSpot虚拟机各方面的运行数据 |
| jinfo | Configuration Info for Java，显示JVM虚拟机的配置信息|
| jmap | Memory Map for Java，生成虚拟机内存转储快照（heapdump文件）|
| jhat | JVM Heap Dump Browser，用于分析heapdump文件，它会建立一个HTTP/HTML服务器，让用户可以在浏览器上查看分析结果
| jstack | Stack Trace for Java，显示虚拟机的线程快照 |
## jps：虚拟机进程状况工具
　　列出当前正在运行的虚拟机进程，并显示虚拟机执行主类名称以及`LVMID`（Local Virtual Machine Identifier，本地虚拟机唯一ID），也可以通过RMI协议查询开启了RMI服务的远程虚拟机进程状态。

| 参数选项 | 作用 |
|:-------------:|:-------------|
| -q | 只显示进程号PID |
| -m | 输出虚拟机进程启动时传递给主类的参数 |
| -l | 输出主类的全名，如果进程执行的是Jar包，输出Jar包的路径|
| -v | 输出虚拟机启动时候的JVM参数|
### `jps -m`
![jps -m](http://img.blog.csdn.net/20170712115857604?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
### `jps -l`
![jps -l](http://img.blog.csdn.net/20170712115945326?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
### `jps -v`
![jps -v](http://img.blog.csdn.net/20170712120018937?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
## [jstat：虚拟机统计信息监视工具](http://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html)
　　用于监视虚拟机各种运行状态信息的命令行工具，可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。

　　`jstat`命令格式为`jstat [option lvmid/vmid [interval[s|ms] [count] ]]`，`interval`和`count`分别是查询间隔和次数，如果省略这两个参数，表示只查询一次。比如`jstat -gc 5700 100 10`表示每100毫秒查询一次GC状况，一共查询10次。

| 参数选项 | 作用 |
|:-------------:|:-------------|
| -class | 监视类装载、卸载数量、总空间以及类装载所耗费的时间 |
| -gc | 监视Java堆状况，包括Eden、Survivor两个区、老年代、永久代等的容量、已用空间、GC时间合计等信息 |
| -gccapacity | 监视内容与-gc基本相同，但输出主要关注Java堆各个区域使用到的最大、最小空间 |
| -gcutil | 监视内容与-gc基本相同，但输出主要关注已使用空间占总空间的百分比 |
| -gccause | 与-gcutil功能一样，但是会额外输出导致上一次GC产生的原因 |
| -gcnew | 监视新生代GC状况 |
| -gcnewcapacity | 监视内容与-gcnew基本相同，输出主要关注使用到的最大、最小空间 |
| -gcold | 监视老年代GC状况 |
| -gcoldcapacity | 监视内容与-gcold基本相同，输出主要关注使用到的最大、最小空间 |
| -compiler | 输出JIT编译器编译过的方法、耗时等信息 |
| -printcompilation | 输出已经被JIT编译的方法 |
### 字段及含义
| 字段| 含义 |
|:-------------:|:-------------|
|S0C|年轻代中第一个survivor（幸存区）的容量 (KB)
|S1C|年轻代中第二个survivor（幸存区）的容量 (KB)
|S0U|年轻代中第一个survivor（幸存区）目前已使用空间 (KB)
|S1U|年轻代中第二个survivor（幸存区）目前已使用空间 (KB)
|EC|年轻代中Eden（伊甸园）的容量 (KB)
|EU|年轻代中Eden（伊甸园）目前已使用空间 (KB)
|OC|Old代的容量 (KB)
|OU|Old代目前已使用空间 (KB)
|PC|Perm(持久代)的容量 (KB)
|PU|Perm(持久代)目前已使用空间 (KB)
|MC|方法区的容量（KB）|
|MU|方法区目前已使用空间（KB）|
|CCSC|压缩类空间大小|
|CCSU|压缩类空间使用大小|
|YGC|从应用程序启动到采样时年轻代中gc次数
|YGCT|从应用程序启动到采样时年轻代中gc所用时间(s)
|FGC|从应用程序启动到采样时old代(全gc)gc次数
|FGCT|从应用程序启动到采样时old代(全gc)gc所用时间(s)
|GCT|从应用程序启动到采样时gc用的总时间(s)
|NGCMN|年轻代(young)中初始化(最小)的大小 (KB)
|NGCMX|年轻代(young)的最大容量 (KB)
|NGC|年轻代(young)中当前的容量 (KB)
|OGCMN|old代中初始化(最小)的大小 (KB) 
|OGCMX|old代的最大容量 (KB)
|OGC|old代当前新生成的容量 (KB)
|PGCMN|perm代中初始化(最小)的大小 (KB) 
|PGCMX|perm代的最大容量 (KB)   
|PGC|perm代当前新生成的容量 (KB)
|S0|年轻代中第一个survivor（幸存区）已使用的占当前容量百分比
|S1|年轻代中第二个survivor（幸存区）已使用的占当前容量百分比
|E|年轻代中Eden（伊甸园）已使用的占当前容量百分比
|O|old代已使用的占当前容量百分比
|P|perm代已使用的占当前容量百分比
|S0CMX|年轻代中第一个survivor（幸存区）的最大容量 (KB)
|S1CMX|年轻代中第二个survivor（幸存区）的最大容量 (KB)
|ECMX|年轻代中Eden（伊甸园）的最大容量 (KB)
|DSS|当前需要survivor（幸存区）的容量 (KB)（Eden区已满）
|TT|持有次数限制
|MTT|最大持有次数限制 
### `jstat -gc`
![jstat -gc](http://img.blog.csdn.net/20170712145841131?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　从结果可以看到，Survivor1和Survivor2的大小都是5.12M，Survivor1暂未被使用，Survivor2使用了4905.2KB，Eden区域大小是33.28M，已使用28.58兆，老年代大小为87.55M，已使用16KB，方法区大小为13.44M，已使用13132.8KB，压缩类空间大小为1664KB，已使用1566.1KB，从程序启动到查看状态时发生了一次Young GC，所用时间为0.007秒，没有发生Full GC，GC总共花费时间为0.007秒。
### `jstat -gcutil`
![jstat -gcutil](http://img.blog.csdn.net/20170712160247834?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　从结果中看到：Survivor1区没使用，Survivor2区使用了95.37%的空间，Eden使用了86.84%的空间，方法区使用了97.77%的空间，压缩类空间使用了93.93%的空间，从程序启动到查看状态时发生了一次Young GC，所用时间为0.007秒，没有发生Full GC，GC总共花费时间为0.007秒。
## jinfo：Java配置信息工具
　　jinfo（Configuration Info for Java）的作用是实时地查看和调整虚拟机各项参数。 使用jps命令的-v参数可以查看虚拟机启动时显式指定的参数列表，但如果想知道未被显式指定的参数的系统默认值，除了去找资料外，就只能使用jinfo的-flag选项进行查询了（JDK 1.6或以上版本的话，使用`java-XX：+PrintFlagsFinal`也可以查看参数默认值），jinfo还可以使用`-sysprops`选项把虚拟机进程的`System.getProperties（）`的内容打印出来。 这个命令在JDK 1.5时期已经随着Linux版的JDK发布，当时只提供了信息查询的功能，JDK 1.6之后，jinfo在Windows和Linux平台都有提供，并且加入了运行期修改参数的能力，可以使用-flag[+|-]name或者-flag name=value修改一部分运行期可写的虚拟机参数值。JDK 1.6中，jinfo对于Windows平台功能仍然有较大限制，只提供了最基本的-flag选项。

　　**注意**：`-Xms`,`-Xmn`系列参数不能通过jinfo指定或打印；jinfo命令作用于`-XX:***`格式的参数
## jmap：Java内存映像工具
　　jmap（Memory Map for Java）命令用于生成堆转储快照（一般称为heapdump或dump文件），然后再用其他可视化工具查看（如jhat）。 如果不使用jmap命令，要想获取Java堆转储快照，还有一些比较“暴力”的手段比如使用`-XX：+HeapDumpOnOutOfMemoryError`参数，可以让虚拟机在OOM异常出现之后自动生成dump文件。

　　jmap的作用并不仅仅是为了获取dump文件，它还可以查询finalize执行队列、 Java堆和永久代的详细信息，如空间使用率、 当前用的是哪种收集器等。和jinfo命令一样，jmap有不少功能在Windows平台下都是受限的，除了生成dump文件的-dump选项和用于查看每个类的实例、 空间占用统计的-histo选项在所有操作系统都提供之外，其余选项都只能在Linux下使用。

| 参数选项 | 作用 |
|:---|:---|
| -dump | -dump:[live,]format=b,file=filename 使用hprof二进制形式，输出jvm的heap内容到文件， live子选项是可选的，假如指定live选项，那么只输出活的对象到文件 |
| -finalizerinfo | 显示在 F-Queue中等待Finalizer线程执行finalize方法的对象，只在Linux平台下有效|
| -heap | 显示Java堆详细信息，如使用哪种回收器、参数配置、分代情况等，只在Linux平台下有效 |
| -histo | 显示堆中对象统计信息，包括类、实例数量、合计容量 |
| -permstat | 以ClassLoader为统计口径显示永久代内存情况，只在Linux平台下有效 |
| -F | 当虚拟机进程对 -dump 选项没有响应时，可使用这个选项强制生成dump快照，只在Linux平台下有效 |
### jmap -dump
![jmap -dump](http://img.blog.csdn.net/20170712173104139?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
### jmap -histo
![jmap -histo](http://img.blog.csdn.net/20170712173446694?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
　　一共有4列数据，分别是编号、个数、字节、类名。
## jhat
　　Sun JDK提供jhat（JVM Heap Analysis Tool）命令与jmap搭配使用，来分析jmap生成的堆转储快照。 jhat内置了一个微型的HTTP/HTML服务器，生成dump文件的分析结果后，可以在浏览器中查看。 

　　在实际工作中，一般不会直接使用jhat命令分析dump文件，一是不会直接在生产环境中分析dump文件，因为这是一个耗时而且消耗硬件资源的过程，二是jhat的分析功能比较简陋（VisualVM、Eclipse Memory Analyzer等工具更专业）。

　　用jhat命令分析刚才生成的dump文件：
![jhat](http://img.blog.csdn.net/20170712174431833?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
　　访问7000端口：
![port7000](http://img.blog.csdn.net/20170712174638216?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
## jstack : Java堆栈跟踪工具
　　jstack（Stack Trace for Java）命令用于生成虚拟机当前时刻的线程快照（一般称为threaddump或者javacore文件）。 线程快照就是当前虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、 死循环、 请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。 线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做些什么事情，或者等待着什么资源。

| 参数选项 | 作用 |
|:---|:---|
| -F | 当正常输出的请求不被响应时，强制输出线程堆栈 |
| -l | 除堆栈外，显示关于锁的附加信息 |
| -m | 如果调用到本地方法的话，可以显示C/C++的堆栈 |
### jstack
![jstack](http://img.blog.csdn.net/20170712195016215?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
可以看到4个线程都处于“WAITING”状态。
### jstack -l
![jstack -l](http://img.blog.csdn.net/20170712195133479?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQWxvbmVfUm9qZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　可以看到线程都没有拥有锁。
