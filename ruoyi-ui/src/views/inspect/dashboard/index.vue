<template>
  <div>
    <!-- ==================== 普通模式 ==================== -->
    <div v-if="!fullscreen" class="app-container">
      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :span="6" v-for="card in statCards" :key="card.key">
          <div :class="['stat-card', card.key]" @click="filterByCard(card)">
            <div class="num">{{ card.value }}</div><div class="label">{{ card.label }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card tenant" @click="$router.push('/inspect/tenant')">
            <div class="num">{{ tenantTotal }}</div><div class="label">租户信息库</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="never">
            <div slot="header" class="clearfix" style="display:flex;justify-content:space-between;align-items:center">
              <b>院落导航</b>
              <el-button type="success" size="mini" icon="el-icon-full-screen" @click="fullscreen=true" style="padding:4px 8px">大屏</el-button>
            </div>
            <div v-for="c in courtyards" :key="c.courtyardId" @click="selectCourtyard(c)" :class="['yard-item',{active:queryParams.courtyardId===c.courtyardId}]">
              <span class="name">{{ c.courtyardName }}</span>
              <span v-if="c.hazardCount" class="badge">{{ c.hazardCount }}</span>
            </div>
            <div @click="selectAll" :class="['yard-item',{active:!queryParams.courtyardId}]">🌐 全辖区</div>
          </el-card>
        </el-col>
        <el-col :span="20">
          <el-card shadow="never">
            <el-form :inline="true" size="small" style="margin-bottom:12px">
              <el-form-item label="巡查类型">
                <el-select v-model="queryParams.mainType" clearable @change="handleQuery()" placeholder="全部">
                  <el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/>
                  <el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/><el-option label="工地" value="site"/>
                </el-select>
              </el-form-item>
              <el-form-item label="细项">
                <el-select v-model="queryParams.subType" clearable @change="handleQuery" placeholder="全部">
                  <el-option label="燃气" value="gas"/><el-option label="消防" value="fire"/>
                  <el-option label="租户" value="tenant"/><el-option label="独居" value="solo"/>
                  <el-option label="门卫" value="guard"/><el-option label="其他" value="other"/>
                </el-select>
              </el-form-item>
              <el-form-item label="巡查结果">
                <el-select v-model="queryParams.inspectResult" clearable @change="handleQuery" placeholder="全部">
                  <el-option label="合格" value="pass"/><el-option label="存在隐患" value="hazard"/><el-option label="已整改" value="rectified"/>
                </el-select>
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
              <el-table-column label="巡查人" prop="createBy" width="100"/>
              <el-table-column label="地址" prop="addressName" min-width="150" show-overflow-tooltip/>
              <el-table-column label="类型" width="110"><template slot-scope="s">{{ typeLabel(s.row.mainType) }}<span v-if="s.row.subType"> / {{ s.row.subType }}</span></template></el-table-column>
              <el-table-column label="级别" width="80"><template slot-scope="s"><el-tag v-if="s.row.hazardLevel" size="mini" :type="s.row.hazardLevel==='critical'?'danger':s.row.hazardLevel==='major'?'warning':'info'">{{ levelMap[s.row.hazardLevel] }}</el-tag><span v-else>-</span></template></el-table-column>
              <el-table-column label="结果" width="90"><template slot-scope="s"><el-tag size="mini" :type="s.row.inspectResult==='pass'?'success':s.row.inspectResult==='rectified'?'info':'danger'">{{ resultMap[s.row.inspectResult] }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="150"><template slot-scope="s"><el-button size="mini" type="text" @click="showDetail(s.row)">详情</el-button><el-button size="mini" type="text" @click="openEdit(s.row)">订正</el-button></template></el-table-column>
            </el-table>
            <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
          </el-card>
        </el-col>
      </el-row>

      <edit-dlg :visible.sync="editVisible" :form="editForm" @submit="submitEdit"/>
      <detail-dlg :visible.sync="detailVisible" :data="detail" :levelMap="levelMap" :resultMap="resultMap"/>
    </div>

    <!-- ==================== 大屏模式 ==================== -->
    <div v-else class="fs-overlay" @keydown.esc="fullscreen=false">
      <aside class="fs-sidebar">
        <div class="fs-sb-head"><h2>院落导航</h2><p>13个指定院落 · 实时监控</p></div>
        <div @click="setGlobalView" :class="['fs-sb-item',{active:!queryParams.courtyardId&&!queryParams.inspectResult&&fsView==='record'}]">🌐 全辖区汇总视图</div>
        <div v-for="c in courtyards" :key="c.courtyardId" :class="['fs-sb-item',{active:queryParams.courtyardId===c.courtyardId&&fsView==='record'}]">
          <div class="fs-sb-name" @click="selectCourtyard(c)">{{ c.courtyardName }}</div>
          <div class="fs-sb-stats">
            <span class="badge-done">已巡 {{ c.totalCount||0 }}</span>
            <span class="badge-urgent" @click.stop="showYardUrgent(c)">隐患 {{ c.hazardCount||0 }}</span>
          </div>
        </div>
      </aside>

      <main class="fs-main">
        <header class="fs-header">
          <div class="fs-header-left">
            <span class="fs-htitle">{{ fsView==='tenant'?'租户信息库':(fsTitle||'全辖区汇总') }}</span>
            <template v-if="fsView==='record'">
              <div class="fs-filter-group">
                <span class="fs-flabel">主载体:</span>
                <select v-model="queryParams.mainType" @change="handleQuery()" class="fs-sel">
                  <option value="">全部类型</option>
                  <option v-for="t in mainTypes" :key="t.value" :value="t.value">{{ t.label }}</option>
                </select>
              </div>
              <div class="fs-filter-group active">
                <span class="fs-flabel">细项:</span>
                <select v-model="queryParams.subType" @change="handleQuery" class="fs-sel">
                  <option value="">全部细项</option>
                  <option v-for="s in buildingSubTypes" :key="s.value" :value="s.value">{{ s.label }}</option>
                </select>
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
        </div>

        <div class="fs-table-wrap">
          <!-- 记录表格 -->
          <table v-if="fsView==='record'" class="fs-table">
            <thead><tr><th>时间</th><th>上传人</th><th>类别/细项</th><th>具体地址</th><th>级别</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="row in logList" :key="row.logId" class="fs-row">
                <td class="fs-td-time">{{ row.inspectTime }}</td>
                <td><span class="fs-worker">{{ row.createBy }}</span></td>
                <td><span class="fs-type">{{ typeLabel(row.mainType) }}</span><span v-if="row.subType"> / {{ row.subType }}</span></td>
                <td class="fs-td-addr">{{ row.addressName }}</td>
                <td><span :class="['fs-lv','lv-'+row.hazardLevel]">{{ levelMap[row.hazardLevel]||'无' }}</span></td>
                <td><span :class="['fs-res',row.inspectResult==='hazard'?'red':row.inspectResult==='rectified'?'green':'gray']">● {{ resultMap[row.inspectResult] }}</span></td>
                <td><button @click="openEdit(row)" class="fs-ebtn">修正详情</button></td>
              </tr>
            </tbody>
          </table>
          <!-- 租户表格 -->
          <table v-else class="fs-table">
            <thead><tr><th>所属院落</th><th>地址</th><th>承租人</th><th>电话</th><th>最近巡查</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="row in tenantList" :key="row.logId" class="fs-row">
                <td class="fs-worker">{{ row.courtyardName }}</td>
                <td class="fs-td-addr">{{ row.addressName }}</td>
                <td class="fs-worker">{{ row.tenantName }}</td>
                <td class="fs-td-time">{{ row.tenantPhone }}</td>
                <td class="fs-td-time">{{ row.inspectTime }}</td>
                <td><button @click="showDetail(row)" class="fs-ebtn">查看详情</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="fs-pgn">
          <span>共 {{ fsView==='record'?total:tenantTotal }} 条</span>
          <div><button :disabled="queryParams.pageNum<=1" @click="queryParams.pageNum--;handleQuery()">上一页</button><button :disabled="queryParams.pageNum*queryParams.pageSize>=total" @click="queryParams.pageNum++;handleQuery()">下一页</button></div>
        </div>
      </main>

      <!-- 大屏弹窗 -->
      <div v-if="editVisible" class="fs-modal" @click.self="editVisible=false">
        <div class="fs-mc"><h3>📋 记录订正 <small>{{ editForm.logCode }}</small></h3>
          <div class="fs-mgrid">
            <div><label>主载体</label><select v-model="editForm.mainType" class="fs-mselect"><option value="building">楼栋</option><option value="street">街巷</option><option value="public">公共场所</option><option value="biz">商企</option><option value="site">工地</option></select></div>
            <div><label>细项</label><input v-model="editForm.subType" class="fs-minput"></div>
            <div><label>结果</label><select v-model="editForm.inspectResult" class="fs-mselect"><option value="pass">合格</option><option value="hazard">存在隐患</option><option value="rectified">已整改</option></select></div>
            <div><label>级别</label><select v-model="editForm.hazardLevel" class="fs-mselect"><option value="">无</option><option value="general">一般</option><option value="major">较大</option><option value="critical">重大</option></select></div>
            <div style="grid-column:1/-1"><label>地址</label><input v-model="editForm.addressName" class="fs-minput"></div>
            <div style="grid-column:1/-1"><label>描述</label><textarea v-model="editForm.description" rows="3" class="fs-minput"></textarea></div>
          </div>
          <div class="fs-mactions"><button @click="editVisible=false">取消</button><button class="primary" @click="submitEdit">确认同步</button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listLog, getLog, updateLog, listTenant } from '@/api/inspect/dashboard'
import { listCourtyard } from '@/api/inspect/dashboard'

// 子组件
const EditDlg = {
  props: { visible: Boolean, form: Object },
  template: `<el-dialog title="记录订正" :visible.sync="show" width="600px">
    <el-form :model="f" label-width="80px" size="small">
      <el-form-item label="巡查类型"><el-select v-model="f.mainType"><el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/><el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/><el-option label="工地" value="site"/></el-select></el-form-item>
      <el-form-item label="巡查项目"><el-input v-model="f.subType"/></el-form-item>
      <el-form-item label="巡查结果"><el-select v-model="f.inspectResult"><el-option label="合格" value="pass"/><el-option label="存在隐患" value="hazard"/><el-option label="已整改" value="rectified"/></el-select></el-form-item>
      <el-form-item label="隐患级别"><el-select v-model="f.hazardLevel"><el-option label="无" value=""/><el-option label="一般" value="general"/><el-option label="较大" value="major"/><el-option label="重大" value="critical"/></el-select></el-form-item>
      <el-form-item label="巡查地址"><el-input v-model="f.addressName"/></el-form-item>
      <el-form-item label="描述"><el-input v-model="f.description" type="textarea" :rows="3"/></el-form-item>
    </el-form><span slot="footer"><el-button @click="show=false">取消</el-button><el-button type="primary" @click="$emit('submit');show=false">确认同步</el-button></span></el-dialog>`,
  computed: { show: { get(){return this.visible}, set(v){this.$emit('update:visible',v)} }, f(){return this.form} }
};

const DetailDlg = {
  props: { visible: Boolean, data: Object, levelMap: Object, resultMap: Object },
  template: `<el-dialog title="日志详情" :visible.sync="show" width="700px"><div v-if="d">
    <p><b>流水号：</b>{{ d.logCode }}</p><p><b>巡查人：</b>{{ d.createBy }} | <b>时间：</b>{{ d.inspectTime }}</p><p><b>地址：</b>{{ d.addressName }}</p>
    <p><b>类型：</b>{{ d.mainType }} / {{ d.subType||'-' }}</p><p><b>结果：</b>{{ resultMap[d.inspectResult] }} | <b>级别：</b>{{ levelMap[d.hazardLevel]||'无' }}</p>
    <p><b>描述：</b>{{ d.description }}</p><p v-if="d.tenantName"><b>租户：</b>{{ d.tenantName }} {{ d.tenantPhone }}</p>
    <div v-if="d.photos&&d.photos.length" style="margin-top:10px"><img v-for="p in d.photos" :key="p.photoId" :src="p.photoUrl" style="width:120px;height:120px;object-fit:cover;margin:5px;border-radius:8px"/></div>
  </div></el-dialog>`,
  computed: { show: { get(){return this.visible}, set(v){this.$emit('update:visible',v)} }, d(){return this.data} }
};

export default {
  name: 'InspectDashboard',
  components: { EditDlg, DetailDlg },
  data() {
    return {
      fullscreen: false, loading: false, total: 0, tenantTotal: 0,
      logList: [], courtyards: [], detail: null, tenantList: [],
      detailVisible: false, editVisible: false, dateRange: [],
      fsView: 'record', tenantCourtyard: '',
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, mainType: '', subType: '', inspectResult: '' },
      editForm: {},
      mainTypes: [{label:'楼栋',value:'building'},{label:'街巷',value:'street'},{label:'公共场所',value:'public'},{label:'商企',value:'biz'},{label:'工地',value:'site'}],
      buildingSubTypes: [{label:'燃气',value:'gas'},{label:'消防',value:'fire'},{label:'租户',value:'tenant'},{label:'独居',value:'solo'},{label:'门卫',value:'guard'},{label:'其他',value:'other'}],
      resultMap: { pass:'合格', hazard:'存在隐患', rectified:'已整改' },
      levelMap: { general:'一般', major:'较大', critical:'重大' },
      typeLabels: { building:'楼栋', street:'街巷', public:'公共场所', biz:'商企', site:'工地' }
    }
  },
  computed: {
    statCards() { return [{key:'total',label:'巡检总量',value:this.total},{key:'hazard',label:'待整改隐患',value:this.hazardTotal},{key:'rectified',label:'已整改完成',value:this.rectifiedTotal}] },
    hazardTotal() { return this.total>0 ? this.logList.filter(l=>l.inspectResult==='hazard').length : 0 },
    rectifiedTotal() { return this.total>0 ? this.logList.filter(l=>l.inspectResult==='rectified').length : 0 },
    fsTitle() {
      const c = this.courtyards.find(x=>x.courtyardId===this.queryParams.courtyardId);
      if (this.queryParams.inspectResult==='hazard') return '待整改隐患';
      if (this.queryParams.inspectResult==='rectified') return '已整改完成';
      return c ? c.courtyardName : '';
    },
    tenantCourtyards() { return [...new Set(this.tenantList.map(t=>t.courtyardName).filter(Boolean))]; }
  },
  watch: { fsView(v) { if(v==='tenant') this.loadTenants(); else this.handleQuery(); } },
  created() { this.loadCourtyards(); this.handleQuery(); },
  mounted() { document.addEventListener('keydown', this.escHandler); },
  beforeDestroy() { document.removeEventListener('keydown', this.escHandler); },
  methods: {
    escHandler(e) { if(e.key==='Escape'&&this.fullscreen) this.fullscreen=false; },
    typeLabel(v) { return this.typeLabels[v]||v; },
    loadCourtyards() { listCourtyard().then(res => { this.courtyards = res.data||[]; }); },
    handleQuery() {
      this.loading = true; this.fsView = 'record';
      const params = { ...this.queryParams };
      if (this.dateRange && this.dateRange.length===2) { params.beginTime = this.dateRange[0]+' 00:00:00'; params.endTime = this.dateRange[1]+' 23:59:59'; }
      listLog(params).then(res => { this.logList = res.rows; this.total = res.total; this.loading = false; });
      this.loadTenants();
    },
    loadTenants() { listTenant({pageNum:1,pageSize:999}).then(res => { this.tenantList = res.rows||[]; this.tenantTotal = res.total||0; }); },
    onDateChange() { this.handleQuery(); },
    selectAll() { this.queryParams.courtyardId=null; this.queryParams.inspectResult=''; this.queryParams.pageNum=1; this.handleQuery(); },
    selectCourtyard(c) { this.queryParams.courtyardId=c.courtyardId; this.queryParams.inspectResult=''; this.queryParams.pageNum=1; this.handleQuery(); },
    setGlobalView() { this.selectAll(); },
    showYardUrgent(c) { this.queryParams.courtyardId=c.courtyardId; this.queryParams.inspectResult='hazard'; this.queryParams.pageNum=1; this.handleQuery(); },
    showAllHazard() { this.queryParams.courtyardId=null; this.queryParams.inspectResult='hazard'; this.queryParams.pageNum=1; this.handleQuery(); },
    showAllRectified() { this.queryParams.courtyardId=null; this.queryParams.inspectResult='rectified'; this.queryParams.pageNum=1; this.handleQuery(); },
    filterByCard(card) { if(card.key==='hazard') this.showAllHazard(); else if(card.key==='rectified') this.showAllRectified(); else this.setGlobalView(); },
    showDetail(row) { getLog(row.logId).then(res => { this.detail = res.data; this.detailVisible = true; }); },
    openEdit(row) { this.editForm = { ...row }; this.editVisible = true; },
    submitEdit() { updateLog(this.editForm).then(() => { this.editVisible = false; this.$message.success('订正成功'); this.handleQuery(); }); },
    handleExport() { this.$message.info('导出: POST /inspect/log/export'); }
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
.yard-item { padding:8px 12px; cursor:pointer; border-left:3px solid transparent; margin:2px 0; font-size:13px; }
.yard-item:hover, .yard-item.active { border-left-color:#409EFF; color:#409EFF; background:#f0f9ff; }
.yard-item .name { font-weight:bold; }
.badge { background:#f56c6c; color:#fff; border-radius:10px; padding:1px 6px; font-size:10px; margin-left:4px; }

/* ====== 大屏模式 ====== */
.fs-overlay { position:fixed; inset:0; z-index:9999; display:flex; background:linear-gradient(135deg,#0f172a 0%,#1e293b 100%); color:#cbd5e1; font-size:13px; }
.fs-sidebar { width:260px; border-right:1px solid rgba(255,255,255,.1); display:flex; flex-direction:column; overflow-y:auto; flex-shrink:0; }
.fs-sb-head { padding:20px; border-bottom:1px solid rgba(255,255,255,.1); }
.fs-sb-head h2 { color:#60a5fa; font-weight:900; font-size:16px; margin:0; }
.fs-sb-head p { color:#64748b; font-size:10px; margin:4px 0 0; text-transform:uppercase; }
.fs-sb-item { padding:12px 16px; cursor:pointer; border-left:4px solid transparent; border-bottom:1px solid rgba(255,255,255,.05); color:#94a3b8; font-size:12px; transition:all .2s; }
.fs-sb-item:hover { background:rgba(255,255,255,.03); }
.fs-sb-item.active { background:rgba(59,130,246,.15) !important; border-left-color:#3b82f6 !important; color:#fff !important; }
.fs-sb-name { font-weight:bold; font-size:13px; display:block; }
.fs-sb-stats { display:flex; gap:8px; margin-top:4px; }
.badge-done { background:rgba(16,185,129,.2); color:#34d399; padding:1px 6px; border-radius:4px; font-size:9px; }
.badge-urgent { background:rgba(239,68,68,.2); color:#f87171; padding:1px 6px; border-radius:4px; font-size:9px; cursor:pointer; }
.badge-urgent:hover { background:rgba(239,68,68,.4); }

.fs-main { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.fs-header { height:64px; border-bottom:1px solid rgba(255,255,255,.1); display:flex; align-items:center; padding:0 24px; background:rgba(15,23,42,.6); flex-shrink:0; gap:12px; }
.fs-header-left { display:flex; align-items:center; gap:12px; flex:1; }
.fs-htitle { font-size:18px; font-weight:bold; color:#fff; white-space:nowrap; }
.fs-filter-group { display:flex; align-items:center; gap:6px; background:#1e293b; border:1px solid #334155; border-radius:8px; padding:4px 10px; }
.fs-filter-group.active { background:rgba(59,130,246,.1); border-color:rgba(59,130,246,.3); }
.fs-flabel { font-size:10px; color:#64748b; font-weight:bold; text-transform:uppercase; }
.fs-sel { background:transparent; border:none; color:#60a5fa; font-size:11px; font-weight:bold; outline:none; cursor:pointer; }
.fs-tchip { background:#1e293b; border:1px solid #334155; color:#94a3b8; padding:4px 12px; border-radius:8px; font-size:11px; cursor:pointer; font-weight:bold; }
.fs-tchip.active, .fs-tchip:hover { background:#f97316; color:#fff; border-color:#f97316; }
.fs-exit { background:rgba(239,68,68,.1); color:#f87171; border:1px solid rgba(239,68,68,.2); padding:6px 16px; border-radius:8px; cursor:pointer; font-size:12px; font-weight:bold; white-space:nowrap; margin-left:auto; }
.fs-exit:hover { background:#ef4444; color:#fff; }

.fs-cards { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; padding:24px; flex-shrink:0; }
.fs-card { padding:24px; border-radius:16px; cursor:pointer; transition:all .2s; }
.fs-card.total { background:rgba(59,130,246,.1); border-top:2px solid #3b82f6; }
.fs-card.hazard { background:rgba(239,68,68,.1); border-top:2px solid #ef4444; }
.fs-card.rectified { background:rgba(16,185,129,.1); border-top:2px solid #10b981; }
.fs-card.tenant { background:rgba(245,158,11,.1); border-left:4px solid #f97316; }
.fs-card:hover { transform:translateY(-2px); }
.fs-card-label { font-size:10px; font-weight:bold; text-transform:uppercase; color:#64748b; margin:0 0 8px; }
.fs-card.total .fs-card-label { color:#60a5fa; }
.fs-card.hazard .fs-card-label { color:#f87171; }
.fs-card.rectified .fs-card-label { color:#34d399; }
.fs-card.tenant .fs-card-label { color:#fb923c; }
.fs-card-num { font-size:36px; font-weight:900; color:#fff; margin:0; font-family:monospace; }
.fs-card.tenant .fs-card-num { color:#fb923c; }

.fs-table-wrap { flex:1; overflow:auto; padding:0 24px 24px; }
.fs-table { width:100%; border-collapse:collapse; font-size:11px; }
.fs-table th { text-align:left; padding:12px 16px; background:rgba(30,41,59,.5); color:#64748b; font-weight:bold; text-transform:uppercase; font-size:10px; position:sticky; top:0; z-index:1; }
.fs-table td { padding:14px 16px; border-bottom:1px solid rgba(255,255,255,.05); }
.fs-row:hover { background:rgba(255,255,255,.03); }
.fs-td-time { color:#94a3b8; font-family:monospace; font-size:11px; }
.fs-worker { color:#fff; font-weight:bold; }
.fs-type { color:#60a5fa; font-weight:bold; }
.fs-td-addr { color:#e2e8f0; font-weight:bold; max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.fs-lv { padding:2px 8px; border-radius:4px; font-size:10px; font-weight:bold; }
.lv-general { background:rgba(59,130,246,.2); color:#60a5fa; border:1px solid rgba(59,130,246,.3); }
.lv-major { background:rgba(245,158,11,.2); color:#fbbf24; }
.lv-critical { background:rgba(239,68,68,.2); color:#f87171; animation:pulse 2s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.6} }
.fs-res.red { color:#f87171; }
.fs-res.green { color:#34d399; }
.fs-res.gray { color:#64748b; }
.fs-ebtn { background:rgba(59,130,246,.15); color:#60a5fa; border:none; padding:4px 12px; border-radius:6px; cursor:pointer; font-size:11px; transition:all .2s; }
.fs-ebtn:hover { background:#3b82f6; color:#fff; }

.fs-pgn { padding:12px 24px; border-top:1px solid rgba(255,255,255,.1); display:flex; justify-content:space-between; align-items:center; font-size:11px; color:#64748b; flex-shrink:0; }
.fs-pgn button { background:#1e293b; border:1px solid #334155; color:#94a3b8; padding:6px 12px; border-radius:6px; cursor:pointer; font-size:11px; margin-left:8px; }
.fs-pgn button:hover:not(:disabled) { background:#3b82f6; color:#fff; border-color:#3b82f6; }
.fs-pgn button:disabled { opacity:.3; cursor:not-allowed; }

.fs-modal { position:fixed; inset:0; z-index:10000; background:rgba(0,0,0,.85); display:flex; align-items:center; justify-content:center; }
.fs-mc { background:#0f172a; border:1px solid #334155; border-radius:24px; padding:32px; width:90%; max-width:600px; max-height:90vh; overflow-y:auto; }
.fs-mc h3 { color:#fff; margin:0 0 20px; font-size:18px; }
.fs-mc h3 small { color:#60a5fa; font-size:12px; }
.fs-mgrid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.fs-mgrid label { display:block; color:#64748b; font-size:10px; font-weight:bold; margin-bottom:4px; text-transform:uppercase; }
.fs-minput, .fs-mselect { width:100%; background:#1e293b; border:1px solid #334155; color:#fff; padding:10px; border-radius:8px; font-size:13px; outline:none; }
.fs-minput:focus, .fs-mselect:focus { border-color:#3b82f6; }
.fs-mactions { display:flex; gap:12px; margin-top:24px; justify-content:flex-end; }
.fs-mactions button { padding:10px 24px; border-radius:8px; font-weight:bold; cursor:pointer; font-size:13px; border:none; }
.fs-mactions button:first-child { background:#1e293b; color:#94a3b8; }
.fs-mactions button.primary { background:#3b82f6; color:#fff; }
</style>
