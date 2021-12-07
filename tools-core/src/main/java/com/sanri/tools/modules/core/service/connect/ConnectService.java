package com.sanri.tools.modules.core.service.connect;

import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ConnectService {
    /**
     * 创建一个连接
     * @param connectInput 连接信息
     */
    public abstract void updateConnect(ConnectInput connectInput) throws IOException;

    /**
     *
     * @param module 模块名
     * @param baseName 基础名
     * @return 查找到的连接
     */
    public abstract ConnectOutput findConnect(String module, String baseName);

    /**
     * 当权限模块加入时, 这个连接列表会按照权限过滤
     * @return 所有连接列表
     */
    public abstract List<ConnectOutput> connectsInternal();

    public List<ConnectOutput> connects(){
        final List<ConnectOutput> connectOutputs = connectsInternal();
        sortConnects(connectOutputs);
        return connectOutputs;
    }

    private void sortConnects(List<ConnectOutput> connectOutputs) {
        // 连接信息排序, 先按模块排序,再按最近访问时间排序
        Collections.sort(connectOutputs, new Comparator<ConnectOutput>() {
            @Override
            public int compare(ConnectOutput o1, ConnectOutput o2) {
                final ConnectInput connectInput1 = o1.getConnectInput();
                final ConnectInput connectInput2 = o2.getConnectInput();
                final int firstCompare = connectInput1.getModule().compareTo(connectInput2.getModule());
                if (firstCompare != 0){
                    return firstCompare;
                }

                return o1.getLastAccessTime().compareTo(o2.getLastAccessTime());
            }
        });
    }

    /**
     * 查询模块的连接列表
     * @param module 模块名
     * @return
     */
    public List<ConnectOutput> moduleConnects(String module){
        final List<ConnectOutput> connects = connects();

        final List<ConnectOutput> filterConnects = connects.stream().filter(connectOutput -> connectOutput.getConnectInput().getModule().equals(module)).collect(Collectors.toList());

        sortConnects(filterConnects);

        return filterConnects;
    }

    /**
     * @return 模块列表
     */
    public abstract List<String> modules();

    /**
     * 创建一个模块
     * @param name 模块名
     */
    public abstract void createModule(String name);

    /**
     * 加载连接信息
     * @return 连接信息的文本内容
     */
    public abstract String loadContent(String module, String baseName) throws IOException;

    /**
     * 删除一个连接
     * @param module 模块名
     * @param baseName 配置名
     */
    public abstract void deleteConnect(String module,String baseName);
}
