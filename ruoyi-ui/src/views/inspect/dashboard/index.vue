<template>
  <div>
    <!-- ==================== 普通模式 ==================== -->
    <div v-if="!fullscreen" class="app-container">
      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :span="4" v-for="card in statCards" :key="card.key">
          <div :class="['stat-card', card.key]" @click="filterByCard(card)">
            <div class="num">{{ card.value }}</div><div class="label">{{ card.label }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card tenant" @click="$router.push({path:'/inspect/tenant',query:{subType:'tenant'}})">
            <div class="num">{{ tenantTotal }}</div><div class="label">租户信息库</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card solo" @click="$router.push({path:'/inspect/tenant',query:{subType:'solo'}})">
            <div class="num">{{ soloTotal }}</div><div class="label">独居信息库</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="never" class="sidebar-card" :style="{minHeight:'500px'}">
            <div slot="header" class="clearfix" style="display:flex;justify-content:space-between;align-items:center">
              <b>网格导航</b>
              <el-button type="success" size="mini" icon="el-icon-full-screen" @click="fullscreen=true" style="padding:4px 8px">大屏</el-button>
            </div>
            <div @click="selectAll" :class="['yard-item',{active:!queryParams.courtyardId&&!queryParams.gridId}]">🌐 全辖区</div>
            <div v-for="grid in gridList" :key="grid.id" class="grid-group">
              <div @click="toggleGrid(grid)" :class="['grid-header',{active:queryParams.gridId===grid.id}]">
                <span class="grid-title">{{ grid.name }}</span>
                <span class="grid-count">{{ grid.children.length }}</span>
                <i :class="['grid-arrow', isGridExpanded(grid.id) ? 'el-icon-arrow-down' : 'el-icon-arrow-right']"></i>
              </div>
              <el-collapse-transition>
                <div v-show="isGridExpanded(grid.id)" class="grid-children">
                  <div v-for="c in grid.children" :key="c.courtyardId" @click="selectCourtyard(c)" :class="['yard-item sub',{active:queryParams.courtyardId===c.courtyardId}]">
                    <span class="name">{{ c.courtyardName }}</span>
                  </div>
                </div>
              </el-collapse-transition>
            </div>
          </el-card>
        </el-col>
        <el-col :span="20">
          <el-card shadow="never">
            <el-form :inline="true" size="small" style="margin-bottom:12px">
              <el-form-item label="巡查类型">
                <el-select v-model="queryParams.mainType" clearable @change="handleQuery()" placeholder="全部">
                  <el-option v-for="d in dictData.placeType" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
                </el-select>
              </el-form-item>
              <el-form-item label="细项">
                <el-select v-model="queryParams.subType" clearable @change="handleQuery" placeholder="全部">
                  <el-option v-for="d in dictData.inspectItem" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
                </el-select>
              </el-form-item>
              <el-form-item label="巡查结果">
                <el-select v-model="queryParams.inspectResult" clearable @change="handleQuery" placeholder="全部">
                  <el-option v-for="d in dictData.inspectResult" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
                </el-select>
              </el-form-item>
              <el-form-item label="巡查人">
                <el-input v-model="queryParams.createBy" clearable placeholder="搜索账号/姓名" @keyup.enter.native="handleQuery" style="width:150px"/>
              </el-form-item>
              <el-form-item>
                <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="yyyy-MM-dd" @change="onDateChange"/>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button>
                <el-button @click="handleExport" icon="el-icon-download">导出</el-button>
              </el-form-item>
            </el-form>

            <el-table v-loading="loading" :data="logList" border stripe>
              <el-table-column label="时间" prop="inspectTime" width="160"/>
              <el-table-column label="巡查人" prop="workerName" width="100"><template slot-scope="s">{{ s.row.workerName || s.row.createBy || '-' }}</template></el-table-column>
              <el-table-column label="地址" prop="addressName" min-width="150" show-overflow-tooltip/>
              <el-table-column label="类型" width="110"><template slot-scope="s">{{ typeLabel(s.row.mainType) }}<span v-if="s.row.subType"> / {{ s.row.subType }}</span></template></el-table-column>
              <el-table-column label="级别" width="80"><template slot-scope="s"><el-tag v-if="s.row.hazardLevel" size="mini" :type="s.row.hazardLevel==='critical'?'danger':s.row.hazardLevel==='major'?'warning':'info'">{{ labelMap.hazardLevel[s.row.hazardLevel] }}</el-tag><span v-else>-</span></template></el-table-column>
              <el-table-column label="结果" width="90"><template slot-scope="s"><el-tag size="mini" :type="s.row.inspectResult==='pass'?'success':s.row.inspectResult==='rectified'?'info':'danger'">{{ labelMap.inspectResult[s.row.inspectResult] }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="150"><template slot-scope="s"><el-button size="mini" type="text" @click="showDetail(s.row)">详情</el-button><el-button size="mini" type="text" @click="openEdit(s.row)">订正</el-button></template></el-table-column>
            </el-table>
            <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
          </el-card>
        </el-col>
      </el-row>

      <el-dialog title="记录订正" :visible.sync="editVisible" width="820px" append-to-body custom-class="inspect-edit-dialog">
        <el-form v-loading="editLoading" :model="editForm" label-width="92px" size="small" class="inspect-edit-form">
          <div class="edit-section">
            <div class="edit-section-title"><i class="el-icon-document"></i> 基础信息</div>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="流水号"><el-input v-model="editForm.logCode" disabled/></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="巡查人"><el-input :value="editForm.workerName || editForm.createBy" disabled/></el-form-item></el-col>
              <el-col :span="12">
                <el-form-item label="巡查时间">
                  <el-date-picker v-model="editForm.inspectTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择巡查时间" style="width:100%"/>
                </el-form-item>
              </el-col>
              <el-col :span="12"><el-form-item label="所属院落"><el-input v-model="editForm.courtyardName" disabled/></el-form-item></el-col>
            </el-row>
          </div>

          <div class="edit-section">
            <div class="edit-section-title"><i class="el-icon-location-outline"></i> 地址与分类</div>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="巡查地址"><el-input v-model="editForm.addressName" placeholder="请输入巡查地址"/></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="详细地址"><el-input v-model="editForm.detailAddress" placeholder="如几栋几单元几号"/></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="巡查类型">
                <el-select v-model="editForm.mainType" clearable placeholder="请选择巡查类型" style="width:100%">
              <el-option v-for="d in dictData.placeType" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="巡查项目">
            <el-select v-model="editForm.subType" clearable placeholder="请选择巡查项目" style="width:100%">
              <el-option v-for="d in dictData.inspectItem" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
              </el-form-item></el-col>
            </el-row>
          </div>

          <div class="edit-section">
            <div class="edit-section-title"><i class="el-icon-warning-outline"></i> 巡查结果</div>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="巡查结果">
            <el-select v-model="editForm.inspectResult" clearable placeholder="请选择巡查结果" style="width:100%">
              <el-option v-for="d in dictData.inspectResult" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="隐患级别">
            <el-select v-model="editForm.hazardLevel" clearable placeholder="请选择隐患级别" style="width:100%">
              <el-option label="无" value=""/>
              <el-option v-for="d in dictData.hazardLevel" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="问题描述"><el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入巡查描述"/></el-form-item></el-col>
            </el-row>
          </div>

          <div class="edit-section">
            <div class="edit-section-title"><i class="el-icon-user"></i> 人员信息</div>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="人员姓名"><el-input v-model="editForm.tenantName" placeholder="租户/独居老人姓名"/></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.tenantPhone" placeholder="请输入联系电话"/></el-form-item></el-col>
            </el-row>
          </div>

          <div class="edit-section">
            <div class="edit-section-title"><i class="el-icon-edit-outline"></i> 备注与照片</div>
            <el-form-item label="备注"><el-input v-model="editForm.remark" type="textarea" :rows="2" placeholder="请输入备注"/></el-form-item>
            <el-form-item label="现场照片">
              <div v-if="editForm.photos && editForm.photos.length" class="edit-photo-list">
                <el-image v-for="p in editForm.photos" :key="p.photoId || p.photoUrl" :src="p.photoUrl" :preview-src-list="editPhotoUrls" fit="cover" class="edit-photo"/>
              </div>
              <el-empty v-else description="暂无现场照片" :image-size="60"/>
            </el-form-item>
          </div>
        </el-form>
        <span slot="footer">
          <el-button @click="editVisible=false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">确认同步</el-button>
        </span>
      </el-dialog>

      <el-dialog title="巡查详情" :visible.sync="detailVisible" width="880px" append-to-body custom-class="inspect-detail-dialog">
        <div v-loading="detailLoading" v-if="detail" class="inspect-detail">
          <div class="detail-hero">
            <div>
              <div class="detail-code">{{ detail.logCode || '未生成流水号' }}</div>
              <div class="detail-address"><i class="el-icon-location"></i> {{ detail.addressName || '-' }}<span v-if="detail.detailAddress"> · {{ detail.detailAddress }}</span></div>
            </div>
            <div class="detail-tags">
              <el-tag :type="detail.inspectResult==='pass'?'success':detail.inspectResult==='rectified'?'info':'danger'" effect="dark">{{ labelMap.inspectResult[detail.inspectResult] || detail.inspectResult || '未知结果' }}</el-tag>
              <el-tag v-if="detail.hazardLevel" :type="detail.hazardLevel==='critical'?'danger':detail.hazardLevel==='major'?'warning':'info'" effect="plain">{{ labelMap.hazardLevel[detail.hazardLevel] || detail.hazardLevel }}</el-tag>
            </div>
          </div>

          <el-row :gutter="16" class="detail-metrics">
            <el-col :span="8"><div class="metric-card"><span>巡查人</span><b>{{ detail.workerName || detail.createBy || '-' }}</b></div></el-col>
            <el-col :span="8"><div class="metric-card"><span>巡查时间</span><b>{{ detail.inspectTime || '-' }}</b></div></el-col>
            <el-col :span="8"><div class="metric-card"><span>所属院落</span><b>{{ detail.courtyardName || '-' }}</b></div></el-col>
          </el-row>

          <div class="detail-section">
            <div class="detail-section-title"><i class="el-icon-s-order"></i> 基本信息</div>
            <el-row :gutter="16">
              <el-col :span="12"><div class="detail-field"><span>巡查类型</span><b>{{ labelMap.placeType[detail.mainType] || detail.mainType || '-' }}</b></div></el-col>
              <el-col :span="12"><div class="detail-field"><span>巡查项目</span><b>{{ labelMap.inspectItem[detail.subType] || detail.subType || '-' }}</b></div></el-col>
              <el-col :span="12"><div class="detail-field"><span>创建时间</span><b>{{ detail.createTime || '-' }}</b></div></el-col>
              <el-col :span="12"><div class="detail-field"><span>更新时间</span><b>{{ detail.updateTime || '-' }}</b></div></el-col>
            </el-row>
          </div>

          <div v-if="detail.tenantName || detail.tenantPhone" class="detail-section person-section">
            <div class="detail-section-title"><i class="el-icon-user"></i> 人员信息</div>
            <el-row :gutter="16">
              <el-col :span="12"><div class="detail-field"><span>人员姓名</span><b>{{ detail.tenantName || '-' }}</b></div></el-col>
              <el-col :span="12"><div class="detail-field"><span>联系电话</span><b>{{ detail.tenantPhone || '-' }}</b></div></el-col>
            </el-row>
          </div>

          <div class="detail-section">
            <div class="detail-section-title"><i class="el-icon-document"></i> 巡查描述</div>
            <div class="detail-desc">{{ detail.description || '暂无描述' }}</div>
            <div v-if="detail.remark" class="detail-remark"><span>备注：</span>{{ detail.remark }}</div>
          </div>

          <div class="detail-section">
            <div class="detail-section-title"><i class="el-icon-picture-outline"></i> 现场照片</div>
            <div v-if="detail.photos && detail.photos.length" class="detail-photo-grid">
              <el-image v-for="p in detail.photos" :key="p.photoId || p.photoUrl" :src="p.photoUrl" :preview-src-list="detailPhotoUrls" fit="cover" class="detail-photo"/>
            </div>
            <el-empty v-else description="暂无现场照片" :image-size="80"/>
          </div>
        </div>
      </el-dialog>
    </div>

    <!-- ==================== 大屏模式 ==================== -->
    <div v-else class="fs-overlay" @keydown.esc="fullscreen=false">
      <aside class="fs-sidebar">
        <div class="fs-sb-head"><h2 style="font-size:24px">网格导航</h2></div>
        <div @click="setGlobalView" :class="['fs-sb-item',{active:!queryParams.courtyardId&&!queryParams.inspectResult&&fsView==='record'}]">🌐 全辖区汇总视图</div>
        <div v-for="c in courtyards" :key="c.courtyardId" :class="['fs-sb-item',{active:queryParams.courtyardId===c.courtyardId&&fsView==='record'}]" @click="selectCourtyard(c)">
          {{ c.courtyardName }}
        </div>
      </aside>

      <main class="fs-main">
        <header class="fs-header">
          <div class="fs-header-left">
            <span class="fs-htitle">{{ fsView==='tenant'?'租户信息库':fsView==='solo'?'独居信息库':(fsTitle||'全辖区汇总') }}</span>
            <template v-if="fsView==='record'">
              <div class="fs-filter-group">
                <span class="fs-flabel">主载体:</span>
                <select v-model="queryParams.mainType" @change="handleQuery()" class="fs-sel">
                  <option value="">全部类型</option>
                  <option v-for="t in dictData.placeType" :key="t.dictValue" :value="t.dictValue">{{ t.dictLabel }}</option>
                </select>
              </div>
              <div class="fs-filter-group">
                <span class="fs-flabel">细项:</span>
                <select v-model="queryParams.subType" @change="handleQuery()" class="fs-sel">
                  <option value="">全部细项</option>
                  <option v-for="s in dictData.inspectItem" :key="s.dictValue" :value="s.dictValue">{{ s.dictLabel }}</option>
                </select>
              </div>
              <div class="fs-filter-group">
                <span class="fs-flabel">结果:</span>
                <select v-model="queryParams.inspectResult" @change="handleQuery()" class="fs-sel">
                  <option value="">全部</option>
                  <option v-for="r in dictData.inspectResult" :key="r.dictValue" :value="r.dictValue">{{ r.dictLabel }}</option>
                </select>
              </div>
              <div class="fs-filter-group">
                <span class="fs-flabel">巡查人:</span>
                <input v-model="queryParams.createBy" class="fs-input" @keyup.enter="handleQuery()" placeholder="搜索巡查人"/>
              </div>
              <div class="fs-filter-group" style="min-width:220px">
                <span class="fs-flabel">时间:</span>
                <input type="date" v-model="fsDateStart" @change="onFsDateChange()" class="fs-input" style="width:110px"/>
                <span style="color:#60a5fa;margin:0 4px">-</span>
                <input type="date" v-model="fsDateEnd" @change="onFsDateChange()" class="fs-input" style="width:110px"/>
              </div>
            </template>
            <template v-if="fsView==='tenant'">
              <button v-for="c in tenantCourtyards" :key="c" @click="tenantCourtyard=c" :class="['fs-tchip',{active:c===tenantCourtyard}]">{{ c }}</button>
            </template>
          </div>
          <button @click="fullscreen=false" class="fs-exit">✕ 退出大屏</button>
        </header>

        <div class="fs-cards">
          <div class="fs-card total" @click="setGlobalView">
            <p class="fs-card-label">巡检总量</p><p class="fs-card-num">{{ total }}</p>
          </div>
          <div class="fs-card hazard" @click="showAllHazard">
            <p class="fs-card-label">待整改隐患</p><p class="fs-card-num">{{ hazardTotal }}</p>
          </div>
          <div class="fs-card rectified" @click="showAllRectified">
            <p class="fs-card-label">已整改完成</p><p class="fs-card-num">{{ rectifiedTotal }}</p>
          </div>
          <div class="fs-card tenant" @click="fsView='tenant';tenantCourtyard=''">
            <p class="fs-card-label">租户信息库</p><p class="fs-card-num">{{ tenantTotal }}</p>
          </div>
          <div class="fs-card solo" @click="fsView='solo';tenantCourtyard=''">
            <p class="fs-card-label">独居信息库</p><p class="fs-card-num">{{ soloTotal }}</p>
          </div>
        </div>

        <div class="fs-table-wrap">
          <!-- 记录表格 -->
          <table v-if="fsView==='record'" class="fs-table">
            <thead><tr><th>时间</th><th>上传人</th><th>类别/细项</th><th>具体地址</th><th>级别</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="row in logList" :key="row.logId" class="fs-row">
                <td class="fs-td-time">{{ row.inspectTime }}</td>
                <td><span class="fs-worker">{{ row.workerName || row.createBy }}</span></td>
                <td><span class="fs-type">{{ typeLabel(row.mainType) }}</span><span v-if="row.subType"> / {{ row.subType }}</span></td>
                <td class="fs-td-addr">{{ row.addressName }}</td>
                <td><span :class="['fs-lv','lv-'+row.hazardLevel]">{{ labelMap.hazardLevel[row.hazardLevel]||'无' }}</span></td>
                <td><span :class="['fs-res',row.inspectResult==='hazard'?'red':row.inspectResult==='rectified'?'green':'gray']">● {{ labelMap.inspectResult[row.inspectResult] }}</span></td>
                <td><button @click="openEdit(row)" class="fs-ebtn">修正详情</button></td>
              </tr>
            </tbody>
          </table>
          <!-- 租户表格 -->
          <table v-if="fsView==='tenant'||fsView==='solo'" class="fs-table">
            <thead><tr><th>所属网格</th><th>地址</th><th>详细地址</th><th>人员</th><th>电话</th><th>最近巡查</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="row in currentPersonList" :key="row.logId" class="fs-row">
                <td class="fs-worker">{{ row.courtyardName }}</td>
                <td class="fs-td-addr">{{ row.addressName }}</td>
                <td class="fs-td-addr">{{ row.detailAddress }}</td>
                <td class="fs-worker">{{ row.tenantName }}</td>
                <td class="fs-td-time">{{ row.tenantPhone }}</td>
                <td class="fs-td-time">{{ row.inspectTime }}</td>
                <td><button @click="showDetail(row)" class="fs-ebtn">查看详情</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="fs-pgn">
          <span>共 {{ fsView==='record'?total:(fsView==='solo'?soloTotal:tenantTotal) }} 条</span>
          <div><button :disabled="queryParams.pageNum<=1" @click="queryParams.pageNum--;handleQuery()">上一页</button><button :disabled="queryParams.pageNum*queryParams.pageSize>=total" @click="queryParams.pageNum++;handleQuery()">下一页</button></div>
        </div>
      </main>

      <!-- 大屏弹窗 -->
      <div v-if="editVisible" class="fs-modal" @click.self="editVisible=false">
        <div class="fs-mc fs-mc-wide">
          <h3>📋 记录订正 <small>{{ editForm.logCode }}</small></h3>
          <div v-if="editLoading" class="fs-loading">详情加载中...</div>
          <template v-else>
            <div class="fs-detail-summary">
              <div><b>巡查人：</b>{{ editForm.workerName || editForm.createBy || '-' }}</div>
              <div><b>时间：</b>{{ editForm.inspectTime || '-' }}</div>
              <div><b>地址：</b>{{ editForm.addressName || '-' }}</div>
              <div v-if="editForm.detailAddress"><b>详细地址：</b>{{ editForm.detailAddress }}</div>
              <div v-if="['tenant','solo'].includes(editForm.subType)" class="fs-person-box">
                <b>{{ editForm.subType==='solo' ? '独居老人' : '租户' }}：</b>{{ editForm.tenantName || '-' }}
                <span v-if="editForm.tenantPhone">（{{ editForm.tenantPhone }}）</span>
              </div>
            </div>
            <div class="fs-mgrid">
              <div><label>主载体</label><select v-model="editForm.mainType" class="fs-mselect"><option v-for="d in dictData.placeType" :key="d.dictValue" :value="d.dictValue">{{ d.dictLabel }}</option></select></div>
              <div><label>细项</label><select v-model="editForm.subType" class="fs-mselect"><option v-for="d in dictData.inspectItem" :key="d.dictValue" :value="d.dictValue">{{ d.dictLabel }}</option></select></div>
              <div><label>结果</label><select v-model="editForm.inspectResult" class="fs-mselect"><option v-for="d in dictData.inspectResult" :key="d.dictValue" :value="d.dictValue">{{ d.dictLabel }}</option></select></div>
              <div><label>级别</label><select v-model="editForm.hazardLevel" class="fs-mselect"><option value="">无</option><option v-for="d in dictData.hazardLevel" :key="d.dictValue" :value="d.dictValue">{{ d.dictLabel }}</option></select></div>
              <div><label>地址</label><input v-model="editForm.addressName" class="fs-minput"></div>
              <div><label>详细地址</label><input v-model="editForm.detailAddress" class="fs-minput"></div>
              <div v-if="['tenant','solo'].includes(editForm.subType)"><label>{{ editForm.subType==='solo' ? '老人姓名' : '承租人' }}</label><input v-model="editForm.tenantName" class="fs-minput"></div>
              <div v-if="['tenant','solo'].includes(editForm.subType)"><label>联系电话</label><input v-model="editForm.tenantPhone" class="fs-minput"></div>
              <div style="grid-column:1/-1"><label>描述</label><textarea v-model="editForm.description" rows="3" class="fs-minput"></textarea></div>
            </div>
            <div v-if="editForm.photos && editForm.photos.length" class="fs-photo-panel">
              <label>现场图片</label>
              <div class="fs-photo-grid">
                <img v-for="p in editForm.photos" :key="p.photoId || p.photoUrl" :src="p.photoUrl" class="fs-photo" />
              </div>
            </div>
            <div v-else class="fs-no-photo">暂无现场图片</div>
          </template>
          <div class="fs-mactions"><button @click="editVisible=false">取消</button><button class="primary" :disabled="editLoading" @click="submitEdit">确认同步</button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listLog, getLog, updateLog, listTenant, exportLog } from '@/api/inspect/dashboard'
import { listCourtyard } from '@/api/inspect/dashboard'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'InspectDashboard',
  data() {
    return {
      fullscreen: false, loading: false, total: 0, tenantTotal: 0, soloTotal: 0,
      logList: [], courtyards: [], detail: null, tenantList: [], soloList: [],
      detailVisible: false, detailLoading: false, editVisible: false, editLoading: false, dateRange: [],
      fsView: 'record', tenantCourtyard: '', expandedGridIds: [],
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, gridId: null, mainType: '', subType: '', inspectResult: '', createBy: '' },
      editForm: {},
      // 字典数据（从数据库加载）
      dictData: { placeType: [], inspectItem: [], inspectResult: [], hazardLevel: [] },
      // 字典映射（供快速查找label）
      labelMap: { placeType: {}, inspectItem: {}, inspectResult: {}, hazardLevel: {} }
    }
  },
  computed: {
    statCards() { return [{key:'total',label:'巡检总量',value:this.total},{key:'hazard',label:'待整改隐患',value:this.hazardTotal},{key:'rectified',label:'已整改完成',value:this.rectifiedTotal}] },
    hazardTotal() { return this.total>0 ? this.logList.filter(l=>l.inspectResult==='hazard').length : 0 },
    rectifiedTotal() { return this.total>0 ? this.logList.filter(l=>l.inspectResult==='rectified').length : 0 },
    fsTitle() {
      const c = this.courtyards.find(x=>x.courtyardId===this.queryParams.courtyardId);
      if (this.queryParams.inspectResult) return this.labelMap.inspectResult[this.queryParams.inspectResult];
      return c ? c.courtyardName : '';
    },
    tenantCourtyards() { return [...new Set(this.tenantList.map(t=>t.courtyardName).filter(Boolean))]; },
    currentPersonList() { return this.fsView==='solo' ? this.soloList : this.tenantList; },
    detailPhotoUrls() { return (this.detail && this.detail.photos ? this.detail.photos : []).map(p => p.photoUrl).filter(Boolean); },
    editPhotoUrls() { return (this.editForm && this.editForm.photos ? this.editForm.photos : []).map(p => p.photoUrl).filter(Boolean); },
    gridList() {
      const map = {};
      this.courtyards.forEach(c => {
        const gid = c.gridDeptId || 0;
        if (!map[gid]) map[gid] = { id: gid, name: c.gridName || '未分组', children: [] };
        map[gid].children.push(c);
      });
      return Object.values(map).sort((a,b) => a.id - b.id);
    }
  },
  watch: { fsView(v) { if (v==='tenant' || v==='solo') this.loadTenants(); else this.handleQuery(); } },
  created() { this.loadDicts(); this.loadCourtyards(); this.handleQuery(); },
  mounted() { document.addEventListener('keydown', this.escHandler); },
  beforeDestroy() { document.removeEventListener('keydown', this.escHandler); },
  methods: {
    escHandler(e) { if(e.key==='Escape'&&this.fullscreen) this.fullscreen=false; },
    // 从数据库字典加载枚举值
    loadDicts() {
      Promise.all([
        getDicts('ins_place_type'),
        getDicts('ins_inspect_item'),
        getDicts('ins_inspect_result'),
        getDicts('ins_hazard_level')
      ]).then(([place, item, result, level]) => {
        this.dictData.placeType = place.data || [];
        this.dictData.inspectItem = item.data || [];
        this.dictData.inspectResult = result.data || [];
        this.dictData.hazardLevel = level.data || [];
        // 构建快速查找映射
        [['placeType', this.dictData.placeType], ['inspectItem', this.dictData.inspectItem],
         ['inspectResult', this.dictData.inspectResult], ['hazardLevel', this.dictData.hazardLevel]]
          .forEach(([key, arr]) => { this.labelMap[key] = {}; arr.forEach(d => { this.labelMap[key][d.dictValue] = d.dictLabel; }); });
      });
    },
    typeLabel(v) { return this.labelMap.placeType[v] || v; },
    isGridExpanded(gridId) { return this.expandedGridIds.includes(gridId); },
    toggleGrid(grid) {
      this.selectGrid(grid);
      if (this.isGridExpanded(grid.id)) {
        this.expandedGridIds = this.expandedGridIds.filter(id => id !== grid.id);
      } else {
        this.expandedGridIds = [...this.expandedGridIds, grid.id];
      }
    },
    loadCourtyards() {
      listCourtyard().then(res => {
        this.courtyards = res.data || [];
        if (!this.expandedGridIds.length && this.gridList.length) {
          this.expandedGridIds = [this.gridList[0].id];
        }
      });
    },
    buildLogQueryParams() {
      const params = { ...this.queryParams };
      if (params.gridId) {
        const cids = this.courtyards.filter(c => c.gridDeptId === params.gridId).map(c => c.courtyardId);
        params.courtyardIds = cids.join(',');
        delete params.gridId;
      } else {
        delete params.gridId;
        delete params.courtyardIds;
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0] + ' 00:00:00';
        params.endTime = this.dateRange[1] + ' 23:59:59';
      }
      return params;
    },
    handleQuery() {
      this.loading = true; this.fsView = 'record';
      const params = this.buildLogQueryParams();
      listLog(params).then(res => { this.logList = res.rows; this.total = res.total; this.loading = false; });
      this.loadTenants();
    },
    getPersonScopeParams() {
      const params = { pageNum: 1, pageSize: 999 };
      if (this.queryParams.courtyardId) {
        params.courtyardId = this.queryParams.courtyardId;
      } else if (this.queryParams.gridId) {
        const courtyardIds = this.courtyards
          .filter(c => c.gridDeptId === this.queryParams.gridId)
          .map(c => c.courtyardId);
        if (courtyardIds.length) params.courtyardIds = courtyardIds.join(',');
      }
      return params;
    },
    loadTenants() {
      const baseParams = this.getPersonScopeParams();
      listTenant({ ...baseParams, subType:'tenant' }).then(res => { this.tenantList = res.rows||[]; this.tenantTotal = res.total||0; });
      listTenant({ ...baseParams, subType:'solo' }).then(res => { this.soloList = res.rows||[]; this.soloTotal = res.total||0; });
    },
    onDateChange() { this.handleQuery(); },
    onFsDateChange() {
      if (this.fsDateStart) this.queryParams.beginTime = this.fsDateStart + ' 00:00:00'; else this.queryParams.beginTime = '';
      if (this.fsDateEnd) this.queryParams.endTime = this.fsDateEnd + ' 23:59:59'; else this.queryParams.endTime = '';
      this.handleQuery();
    },
    selectAll() { this.queryParams.courtyardId=null; this.queryParams.gridId=null; this.queryParams.inspectResult=''; this.queryParams.pageNum=1; this.handleQuery(); },
    selectGrid(g) { this.queryParams.gridId=g.id; this.queryParams.courtyardId=null; this.queryParams.inspectResult=''; this.queryParams.pageNum=1; this.handleQuery(); },
    selectCourtyard(c) {
      this.queryParams.courtyardId=c.courtyardId;
      this.queryParams.gridId=null;
      this.queryParams.inspectResult='';
      this.queryParams.pageNum=1;
      const grid = this.gridList.find(g => g.children.some(item => item.courtyardId === c.courtyardId));
      if (grid && !this.isGridExpanded(grid.id)) this.expandedGridIds = [...this.expandedGridIds, grid.id];
      this.handleQuery();
    },
    setGlobalView() { this.selectAll(); },
    showYardUrgent(c) { this.queryParams.courtyardId=c.courtyardId; this.queryParams.inspectResult='hazard'; this.queryParams.pageNum=1; this.handleQuery(); },
    showAllHazard() { this.queryParams.courtyardId=null; this.queryParams.inspectResult='hazard'; this.queryParams.pageNum=1; this.handleQuery(); },
    showAllRectified() { this.queryParams.courtyardId=null; this.queryParams.inspectResult='rectified'; this.queryParams.pageNum=1; this.handleQuery(); },
    filterByCard(card) { if(card.key==='hazard') this.showAllHazard(); else if(card.key==='rectified') this.showAllRectified(); else this.setGlobalView(); },
    showDetail(row) {
      if (!row || !row.logId) {
        this.$message.error('当前记录缺少日志ID，无法查看详情');
        return;
      }
      this.detail = { ...row };
      this.detailVisible = true;
      this.detailLoading = true;
      getLog(row.logId).then(res => {
        this.detail = res.data || row;
      }).catch(() => {
        this.$message.error('详情加载失败，请检查接口或权限');
      }).finally(() => { this.detailLoading = false; });
    },
    openEdit(row) {
      if (!row || !row.logId) {
        this.$message.error('当前记录缺少日志ID，无法订正');
        return;
      }
      this.editVisible = true;
      this.editLoading = true;
      this.editForm = { ...row };
      getLog(row.logId).then(res => {
        this.editForm = res.data || row;
      }).catch(() => {
        this.$message.warning('详情加载失败，已使用列表数据订正');
      }).finally(() => { this.editLoading = false; });
    },
    submitEdit() { updateLog(this.editForm).then(() => { this.editVisible = false; this.$message.success('订正成功'); this.handleQuery(); }); },
    handleExport() {
      const params = this.buildLogQueryParams();
      delete params.pageNum;
      delete params.pageSize;
      exportLog(params);
    }
  }
}
</script>

<style scoped>
/* ====== 普通模式 ====== */
.stat-card { background:#fff; border-radius:12px; padding:20px; text-align:center; cursor:pointer; box-shadow:0 2px 8px rgba(0,0,0,.06); border:2px solid transparent; transition:all .3s; }
.stat-card:hover { border-color:#409EFF; }
.stat-card .num { font-size:32px; font-weight:bold; color:#303133; }
.stat-card .label { font-size:12px; color:#909399; margin-top:4px; }
.stat-card.tenant { border-left:4px solid #e6a23c; }
.stat-card.tenant:hover { border-color:#e6a23c; background:#fef0d2; }
.sidebar-card { position:sticky; top:0; }
.grid-group { margin-bottom:6px; border:1px solid #ebeef5; border-radius:8px; overflow:hidden; background:#fff; }
.grid-header { display:flex; align-items:center; gap:6px; padding:9px 10px; cursor:pointer; font-weight:bold; font-size:13px; color:#303133; border-left:3px solid transparent; background:#fafafa; transition:all .2s; }
.grid-header:hover, .grid-header.active { color:#409EFF; background:#f0f9ff; border-left-color:#409EFF; }
.grid-title { flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.grid-count { min-width:20px; height:18px; line-height:18px; text-align:center; border-radius:9px; background:#edf2f7; color:#909399; font-size:11px; }
.grid-header.active .grid-count { background:#409EFF; color:#fff; }
.grid-arrow { font-size:12px; color:#909399; transition:transform .2s; }
.grid-children { padding:4px 0 6px; background:#fff; }
.yard-item { padding:8px 12px; cursor:pointer; border-left:3px solid transparent; margin:2px 0; font-size:13px; }
.yard-item.sub { padding-left:28px; font-size:12px; color:#606266; }
.yard-item:hover, .yard-item.active { border-left-color:#409EFF; color:#409EFF; background:#f0f9ff; }
.yard-item .name { font-weight:bold; }
.badge { background:#f56c6c; color:#fff; border-radius:10px; padding:1px 6px; font-size:10px; margin-left:4px; }

::v-deep .inspect-edit-dialog .el-dialog__body,
::v-deep .inspect-detail-dialog .el-dialog__body { padding:18px 22px 22px; background:#f5f7fa; }
::v-deep .inspect-edit-dialog .el-dialog__header,
::v-deep .inspect-detail-dialog .el-dialog__header { border-bottom:1px solid #ebeef5; padding:16px 22px; }
.inspect-edit-form { max-height:68vh; overflow-y:auto; padding-right:4px; }
.edit-section,
.detail-section { background:#fff; border:1px solid #ebeef5; border-radius:12px; padding:16px; margin-bottom:14px; box-shadow:0 2px 8px rgba(0,0,0,.03); }
.edit-section-title,
.detail-section-title { display:flex; align-items:center; gap:6px; margin-bottom:14px; font-size:15px; font-weight:700; color:#303133; }
.edit-section-title i,
.detail-section-title i { color:#409EFF; }
.edit-photo-list { display:grid; grid-template-columns:repeat(auto-fill,96px); gap:10px; }
.edit-photo { width:96px; height:96px; border-radius:8px; overflow:hidden; border:1px solid #ebeef5; }
.inspect-detail { min-height:240px; }
.detail-hero { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; padding:20px; margin-bottom:16px; color:#fff; border-radius:14px; background:linear-gradient(135deg,#2563eb 0%,#0f766e 100%); box-shadow:0 8px 20px rgba(37,99,235,.22); }
.detail-code { font-size:22px; font-weight:800; letter-spacing:.5px; margin-bottom:10px; }
.detail-address { font-size:14px; opacity:.95; }
.detail-tags { display:flex; gap:8px; flex-wrap:wrap; justify-content:flex-end; }
.detail-metrics { margin-bottom:14px; }
.metric-card { background:#fff; border:1px solid #ebeef5; border-radius:12px; padding:14px; min-height:76px; box-shadow:0 2px 8px rgba(0,0,0,.03); }
.metric-card span,
.detail-field span { display:block; color:#909399; font-size:12px; margin-bottom:8px; }
.metric-card b,
.detail-field b { color:#303133; font-size:14px; font-weight:700; word-break:break-all; }
.detail-field { padding:12px 14px; border-radius:10px; background:#f8fafc; margin-bottom:10px; }
.person-section { border-color:#f3d19e; background:#fffaf0; }
.detail-desc { min-height:64px; padding:14px; color:#303133; line-height:1.7; white-space:pre-wrap; word-break:break-all; border-radius:10px; background:#f8fafc; }
.detail-remark { margin-top:10px; padding:10px 12px; border-left:3px solid #e6a23c; background:#fdf6ec; color:#606266; border-radius:6px; }
.detail-remark span { color:#e6a23c; font-weight:700; }
.detail-photo-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(130px,1fr)); gap:12px; }
.detail-photo { width:100%; height:130px; border-radius:10px; overflow:hidden; border:1px solid #ebeef5; background:#f5f7fa; }

/* ====== 大屏模式 ====== */
.fs-overlay { position:fixed; inset:0; z-index:9999; display:flex; background:linear-gradient(135deg,#0f172a 0%,#1e293b 100%); color:#cbd5e1; font-size:16px; }
.fs-sidebar { width:240px; border-right:1px solid rgba(255,255,255,.1); display:flex; flex-direction:column; overflow-y:auto; flex-shrink:0; background:#0b1220; }
.fs-sb-head { padding:18px; border-bottom:1px solid rgba(255,255,255,.1); background:#08111f; }
.fs-sb-head h2 { color:#93c5fd; font-weight:900; font-size:24px; margin:0; }
.fs-sb-head p { color:#64748b; font-size:10px; margin:4px 0 0; text-transform:uppercase; }
.fs-sb-item { padding:14px 16px; cursor:pointer; border-left:4px solid transparent; border-bottom:1px solid rgba(255,255,255,.06); color:#cbd5e1; font-size:16px; transition:all .2s; background:#101a2c; }
.fs-sb-item:hover { background:#17233a; }
.fs-sb-item.active { background:rgba(59,130,246,.15) !important; border-left-color:#3b82f6 !important; color:#fff !important; }
.fs-sb-name { font-weight:bold; font-size:13px; display:block; }
.fs-sb-stats { display:flex; gap:8px; margin-top:4px; }
.badge-done { background:rgba(16,185,129,.2); color:#34d399; padding:1px 6px; border-radius:4px; font-size:9px; }
.badge-urgent { background:rgba(239,68,68,.2); color:#f87171; padding:1px 6px; border-radius:4px; font-size:9px; cursor:pointer; }
.badge-urgent:hover { background:rgba(239,68,68,.4); }

.fs-main { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.fs-header { min-height:112px; border-bottom:1px solid rgba(255,255,255,.1); display:flex; align-items:flex-start; padding:12px 24px; background:rgba(15,23,42,.6); flex-shrink:0; gap:14px; }
.fs-header-left { display:flex; align-items:center; gap:10px; flex:1; flex-wrap:wrap; min-width:0; }
.fs-htitle { font-size:26px; font-weight:bold; color:#fff; white-space:nowrap; flex-basis:100%; margin-bottom:4px; }
.fs-filter-group { display:flex; align-items:center; gap:6px; background:#1e293b; border:1px solid #334155; border-radius:8px; padding:6px 10px; flex:0 0 auto; }
.fs-filter-group.active { background:rgba(59,130,246,.1); border-color:rgba(59,130,246,.3); }
.fs-flabel { font-size:14px; color:#64748b; font-weight:bold; text-transform:uppercase; }
.fs-sel { background:transparent; border:none; color:#60a5fa; font-size:15px; font-weight:bold; outline:none; cursor:pointer; }
.fs-tchip { background:#1e293b; border:1px solid #334155; color:#94a3b8; padding:4px 12px; border-radius:8px; font-size:11px; cursor:pointer; font-weight:bold; }
.fs-tchip.active, .fs-tchip:hover { background:#f97316; color:#fff; border-color:#f97316; }
.fs-exit { background:rgba(239,68,68,.1); color:#f87171; border:1px solid rgba(239,68,68,.2); padding:10px 18px; border-radius:10px; cursor:pointer; font-size:16px; font-weight:bold; white-space:nowrap; margin-left:auto; margin-top:2px; }
.fs-exit:hover { background:#ef4444; color:#fff; }

.fs-cards { display:grid; grid-template-columns:repeat(5,minmax(0,1fr)); gap:14px; padding:18px 24px; flex-shrink:0; }
.fs-card { padding:18px 20px; border-radius:16px; cursor:pointer; transition:all .2s; min-width:0; }
.fs-card.total { background:rgba(59,130,246,.1); border-top:2px solid #3b82f6; }
.fs-card.hazard { background:rgba(239,68,68,.1); border-top:2px solid #ef4444; }
.fs-card.rectified { background:rgba(16,185,129,.1); border-top:2px solid #10b981; }
.fs-card.tenant { background:rgba(245,158,11,.14); border-top:2px solid #f97316; }
.fs-card:hover { transform:translateY(-2px); }
.fs-card-label { font-size:16px; font-weight:bold; text-transform:uppercase; color:#64748b; margin:0 0 10px; }
.fs-card.total .fs-card-label { color:#60a5fa; }
.fs-card.hazard .fs-card-label { color:#f87171; }
.fs-card.rectified .fs-card-label { color:#34d399; }
.fs-card.tenant .fs-card-label { color:#fb923c; }
.fs-card.solo { background:rgba(168,85,247,.14); border-top:2px solid #a855f7; }
.fs-card.solo .fs-card-label { color:#c084fc; }
.fs-card-num { font-size:46px; font-weight:900; color:#fff; margin:0; font-family:monospace; line-height:1; }
.fs-card.tenant .fs-card-num { color:#fb923c; }
.fs-card.solo .fs-card-num { color:#c084fc; }

.fs-table-wrap { flex:1; overflow:auto; padding:0 24px 24px; }
.fs-table { width:100%; border-collapse:collapse; font-size:16px; }
.fs-table th { text-align:left; padding:16px 18px; background:rgba(30,41,59,.5); color:#64748b; font-weight:bold; text-transform:uppercase; font-size:14px; position:sticky; top:0; z-index:1; }
.fs-table td { padding:18px 18px; border-bottom:1px solid rgba(255,255,255,.05); }
.fs-row:hover { background:rgba(255,255,255,.03); }
.fs-td-time { color:#94a3b8; font-family:monospace; font-size:15px; }
.fs-worker { color:#fff; font-weight:bold; }
.fs-type { color:#60a5fa; font-weight:bold; }
.fs-td-addr { color:#e2e8f0; font-weight:bold; max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.fs-lv { padding:4px 10px; border-radius:6px; font-size:14px; font-weight:bold; }
.lv-general { background:rgba(59,130,246,.2); color:#60a5fa; border:1px solid rgba(59,130,246,.3); }
.lv-major { background:rgba(245,158,11,.2); color:#fbbf24; }
.lv-critical { background:rgba(239,68,68,.2); color:#f87171; animation:pulse 2s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.6} }
.fs-res.red { color:#f87171; }
.fs-res.green { color:#34d399; }
.fs-res.gray { color:#64748b; }
.fs-ebtn { background:rgba(59,130,246,.15); color:#60a5fa; border:none; padding:8px 16px; border-radius:8px; cursor:pointer; font-size:15px; transition:all .2s; }
.fs-ebtn:hover { background:#3b82f6; color:#fff; }

.fs-pgn { padding:16px 28px; border-top:1px solid rgba(255,255,255,.1); display:flex; justify-content:space-between; align-items:center; font-size:15px; color:#64748b; flex-shrink:0; }
.fs-pgn button { background:#1e293b; border:1px solid #334155; color:#94a3b8; padding:8px 16px; border-radius:8px; cursor:pointer; font-size:15px; margin-left:10px; }
.fs-pgn button:hover:not(:disabled) { background:#3b82f6; color:#fff; border-color:#3b82f6; }
.fs-pgn button:disabled { opacity:.3; cursor:not-allowed; }

.fs-modal { position:fixed; inset:0; z-index:10000; background:rgba(0,0,0,.85); display:flex; align-items:center; justify-content:center; }
.fs-mc { background:#0f172a; border:1px solid #334155; border-radius:24px; padding:32px; width:90%; max-width:600px; max-height:90vh; overflow-y:auto; }
.fs-mc h3 { color:#fff; margin:0 0 20px; font-size:18px; }
.fs-mc h3 small { color:#60a5fa; font-size:12px; }
.fs-mgrid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.fs-mgrid label { display:block; color:#64748b; font-size:10px; font-weight:bold; margin-bottom:4px; text-transform:uppercase; }
.fs-minput, .fs-mselect { width:100%; background:#1e293b; border:1px solid #334155; color:#fff; padding:12px; border-radius:8px; font-size:16px; outline:none; }
.fs-minput:focus, .fs-mselect:focus { border-color:#3b82f6; }
.fs-mactions { display:flex; gap:12px; margin-top:24px; justify-content:flex-end; }
.fs-mactions button { padding:10px 24px; border-radius:8px; font-weight:bold; cursor:pointer; font-size:13px; border:none; }
.fs-mactions button:first-child { background:#1e293b; color:#94a3b8; }
.fs-mactions button.primary { background:#3b82f6; color:#fff; }
.fs-mc-wide { max-width:920px; }
.fs-loading, .fs-no-photo { color:#94a3b8; background:#111827; border:1px dashed #334155; border-radius:10px; padding:18px; text-align:center; margin-bottom:16px; }
.fs-detail-summary { display:grid; grid-template-columns:1fr 1fr; gap:10px 16px; color:#cbd5e1; background:#111827; border:1px solid #334155; border-radius:12px; padding:14px; margin-bottom:16px; font-size:15px; }
.fs-detail-summary b { color:#93c5fd; }
.fs-person-box { grid-column:1/-1; color:#fbbf24; }
.fs-photo-panel { margin-top:18px; }
.fs-photo-panel label { display:block; color:#64748b; font-size:14px; font-weight:bold; margin-bottom:10px; }
.fs-photo-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(140px,1fr)); gap:12px; }
.fs-photo { width:100%; height:140px; object-fit:cover; border-radius:10px; border:1px solid #334155; background:#111827; }
</style>
