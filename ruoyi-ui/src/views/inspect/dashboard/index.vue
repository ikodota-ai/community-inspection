<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <div @click="filterByCard(card)" :class="['stat-card', { active: card.active }]">
          <div class="num">{{ card.value }}</div>
          <div class="label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧院落导航 -->
      <el-col :span="4">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><b>院落导航</b></div>
          <div v-for="c in courtyards" :key="c.courtyardId" 
               @click="selectCourtyard(c)" 
               :class="['yard-item', { active: queryParams.courtyardId === c.courtyardId }]">
            <span class="name">{{ c.courtyardName }}</span>
            <el-badge :value="c.hazardCount||0" class="badge" v-if="c.hazardCount"/>
          </div>
          <div @click="queryParams.courtyardId=null;handleQuery()" :class="['yard-item', { active: !queryParams.courtyardId }]">
            🌐 全辖区
          </div>
        </el-card>
      </el-col>

      <!-- 右侧表格 -->
      <el-col :span="20">
        <el-card shadow="never">
          <!-- 筛选栏 -->
          <el-form :inline="true" :model="queryParams" size="small">
            <el-form-item label="巡查类型">
              <el-select v-model="queryParams.mainType" clearable @change="handleQuery" placeholder="全部">
                <el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/>
                <el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/>
                <el-option label="工地" value="site"/>
              </el-select>
            </el-form-item>
            <el-form-item label="巡查项目" v-if="queryParams.mainType==='building'">
              <el-select v-model="queryParams.subType" clearable @change="handleQuery" placeholder="全部">
                <el-option label="燃气" value="gas"/><el-option label="消防" value="fire"/>
                <el-option label="租户" value="tenant"/><el-option label="独居" value="solo"/>
                <el-option label="门卫" value="guard"/><el-option label="其他" value="other"/>
              </el-select>
            </el-form-item>
            <el-form-item label="巡查结果">
              <el-select v-model="queryParams.inspectResult" clearable @change="handleQuery" placeholder="全部">
                <el-option label="合格" value="pass"/><el-option label="存在隐患" value="hazard"/>
                <el-option label="已整改" value="rectified"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="yyyy-MM-dd HH:mm:ss" @change="handleQuery"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button>
              <el-button @click="handleExport" icon="el-icon-download">导出</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="logList" border stripe>
            <el-table-column label="时间" prop="inspectTime" width="160"/>
            <el-table-column label="巡查人" prop="createBy" width="100"/>
            <el-table-column label="地址" prop="addressName" min-width="150" show-overflow-tooltip/>
            <el-table-column label="类型" width="100">
              <template slot-scope="scope">{{ scope.row.mainType }} / {{ scope.row.subType }}</template>
            </el-table-column>
            <el-table-column label="级别" width="80">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.hazardLevel" size="mini" 
                  :type="scope.row.hazardLevel==='critical'?'danger':scope.row.hazardLevel==='major'?'warning':'info'">
                  {{ levelMap[scope.row.hazardLevel] }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="结果" width="90">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.inspectResult==='pass'?'success':scope.row.inspectResult==='rectified'?'info':'danger'">
                  {{ resultMap[scope.row.inspectResult] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="showDetail(scope.row)">详情</el-button>
                <el-button size="mini" type="text" @click="openEdit(scope.row)">订正</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订正弹窗 -->
    <el-dialog title="记录订正" :visible.sync="editVisible" width="600px">
      <el-form :model="editForm" label-width="80px" size="small">
        <el-form-item label="巡查类型"><el-select v-model="editForm.mainType"><el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/><el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/><el-option label="工地" value="site"/></el-select></el-form-item>
        <el-form-item label="巡查项目"><el-input v-model="editForm.subType"/></el-form-item>
        <el-form-item label="巡查结果"><el-select v-model="editForm.inspectResult"><el-option label="合格" value="pass"/><el-option label="存在隐患" value="hazard"/><el-option label="已整改" value="rectified"/></el-select></el-form-item>
        <el-form-item label="隐患级别"><el-select v-model="editForm.hazardLevel"><el-option label="无" value=""/><el-option label="一般" value="general"/><el-option label="较大" value="major"/><el-option label="重大" value="critical"/></el-select></el-form-item>
        <el-form-item label="巡查地址"><el-input v-model="editForm.addressName"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="editVisible=false">取消</el-button><el-button type="primary" @click="submitEdit">确认同步</el-button></span>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="日志详情" :visible.sync="detailVisible" width="700px">
      <div v-if="detail">
        <p><b>流水号：</b>{{ detail.logCode }}</p>
        <p><b>巡查人：</b>{{ detail.createBy }} | <b>时间：</b>{{ detail.inspectTime }}</p>
        <p><b>地址：</b>{{ detail.addressName }}</p>
        <p><b>类型：</b>{{ detail.mainType }} / {{ detail.subType }}</p>
        <p><b>结果：</b>{{ resultMap[detail.inspectResult] }} | <b>级别：</b>{{ levelMap[detail.hazardLevel]||'无' }}</p>
        <p><b>描述：</b>{{ detail.description }}</p>
        <p v-if="detail.tenantName"><b>租户：</b>{{ detail.tenantName }} {{ detail.tenantPhone }}</p>
        <div v-if="detail.photos&&detail.photos.length" style="margin-top:10px">
          <img v-for="p in detail.photos" :key="p.photoId" :src="p.photoUrl" style="width:120px;height:120px;object-fit:cover;margin:5px;border-radius:8px"/>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLog, getLog, updateLog, getStatistics } from '@/api/inspect/dashboard'
import { listCourtyard } from '@/api/inspect/dashboard'

export default {
  name: 'InspectDashboard',
  data() {
    return {
      loading: false, total: 0,
      logList: [], courtyards: [], detail: null,
      detailVisible: false, editVisible: false,
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, mainType: null, subType: null, inspectResult: null },
      editForm: {},
      resultMap: { pass: '合格', hazard: '存在隐患', rectified: '已整改' },
      levelMap: { general: '一般', major: '较大', critical: '重大' }
    }
  },
  computed: {
    statCards() {
      const total = this.total;
      const hazard = this.logList.filter(l=>l.inspectResult==='hazard').length;
      const rectified = this.logList.filter(l=>l.inspectResult==='rectified').length;
      return [
        { key:'total', label:'巡检总量', value:total, active:false },
        { key:'hazard', label:'待整改隐患', value:hazard, active:false },
        { key:'rectified', label:'已整改完成', value:rectified, active:false }
      ]
    }
  },
  created() { this.loadCourtyards(); this.handleQuery(); },
  methods: {
    loadCourtyards() {
      listCourtyard().then(res => { this.courtyards = res.data || []; });
    },
    handleQuery() {
      this.loading = true;
      const params = { ...this.queryParams };
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0];
        params.endTime = this.dateRange[1];
      }
      listLog(params).then(res => { this.logList = res.rows; this.total = res.total; this.loading = false; });
    },
    selectCourtyard(c) { this.queryParams.courtyardId = c.courtyardId; this.handleQuery(); },
    filterByCard(card) { /* simple: highlight card */ },
    showDetail(row) { getLog(row.logId).then(res => { this.detail = res.data; this.detailVisible = true; }); },
    openEdit(row) { this.editForm = { ...row }; this.editVisible = true; },
    submitEdit() { updateLog(this.editForm).then(() => { this.editVisible = false; this.$message.success('订正成功'); this.handleQuery(); }); },
    handleExport() { this.$message.info('导出功能通过API调用 POST /inspect/log/export'); }
  }
}
</script>

<style scoped>
.stat-card { background: #fff; border-radius: 12px; padding: 20px; text-align: center; cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 2px solid transparent; transition: all .3s; }
.stat-card:hover, .stat-card.active { border-color: #409EFF; }
.stat-card .num { font-size: 32px; font-weight: bold; color: #303133; }
.stat-card .label { font-size: 12px; color: #909399; margin-top: 4px; }
.yard-item { padding: 8px 12px; cursor: pointer; border-left: 3px solid transparent; margin: 2px 0; font-size:13px; }
.yard-item:hover, .yard-item.active { border-left-color: #409EFF; color: #409EFF; background: #f0f9ff; }
.yard-item .name { font-weight: bold; }
.badge { margin-left: 6px; }
</style>
