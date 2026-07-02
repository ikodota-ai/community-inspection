<template>
  <div class="app-container">
    <h2 style="margin-bottom:15px">特殊人员档案</h2>
    <el-form :inline="true" :model="queryParams" size="small">
      <el-form-item label="网格"><el-select v-model="queryParams.courtyardId" clearable @change="handleQuery" placeholder="全部"><el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/></el-select></el-form-item>
      <el-form-item label="人员类型"><el-select v-model="queryParams.subType" clearable @change="handleQuery" placeholder="全部">
        <el-option label="租户" value="tenant"/>
        <el-option label="独居老人" value="solo"/>
      </el-select></el-form-item>
      <el-form-item label="姓名"><el-input v-model="queryParams.tenantName" clearable placeholder="搜索姓名" @keyup.enter.native="handleQuery"/></el-form-item>
      <el-form-item label="电话"><el-input v-model="queryParams.tenantPhone" clearable placeholder="搜索电话" @keyup.enter.native="handleQuery"/></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="网格" prop="courtyardName" width="120"/>
      <el-table-column label="地址" prop="addressName" min-width="150"/>
      <el-table-column label="详细地址" prop="detailAddress" min-width="150"/>
      <el-table-column label="人员类型" prop="subType" width="100">
        <template slot-scope="s"><el-tag size="mini" :type="s.row.subType==='tenant'?'primary':'success'">{{ s.row.subType==='tenant'?'租户':'独居老人' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="姓名" prop="tenantName" width="100"/>
      <el-table-column label="电话" prop="tenantPhone" width="130"/>
      <el-table-column label="最近巡查时间" prop="inspectTime" width="160"/>
      <el-table-column label="最近结果" width="90">
        <template slot-scope="s"><el-tag size="mini" :type="s.row.inspectResult==='pass'?'success':'danger'">{{ resultMap[s.row.inspectResult] }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="s">
          <el-button size="mini" type="text" @click="showDetail(s.row)">详情</el-button>
          <el-button size="mini" type="text" @click="openEdit(s.row)">订正</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 详情弹窗 -->
    <el-dialog title="巡查详情" :visible.sync="detailVisible" width="600px" append-to-body>
      <div v-if="detail" style="padding:10px">
        <p><b>流水号：</b>{{ detail.logCode }}</p>
        <p><b>巡查人：</b>{{ detail.createBy }} | <b>时间：</b>{{ detail.inspectTime }}</p>
        <p><b>地址：</b>{{ detail.addressName }}</p>
        <p v-if="detail.detailAddress"><b>详细地址：</b>{{ detail.detailAddress }}</p>
        <p><b>类型：</b>{{ detail.mainType }} / {{ detail.subType || '-' }}</p>
        <p><b>结果：</b>{{ resultMap[detail.inspectResult] }} | <b>级别：</b>{{ levelMap[detail.hazardLevel] || '无' }}</p>
        <p><b>描述：</b>{{ detail.description || '-' }}</p>
        <p v-if="detail.tenantName"><b>人员：</b>{{ detail.tenantName }} {{ detail.tenantPhone || '' }}</p>
        <div v-if="detail.photos && detail.photos.length" style="margin-top:10px">
          <b>现场照片：</b><br>
          <img v-for="p in detail.photos" :key="p.photoId" :src="p.photoUrl" style="width:120px;height:120px;object-fit:cover;margin:5px;border-radius:8px">
        </div>
      </div>
      <div slot="footer"><el-button @click="detailVisible=false">关闭</el-button></div>
    </el-dialog>
    <!-- 订正弹窗 -->
    <el-dialog title="订正巡查记录" :visible.sync="editVisible" width="600px" append-to-body>
      <el-form :model="editForm" label-width="80px" size="small">
        <el-form-item label="巡查类型"><el-select v-model="editForm.mainType" clearable><el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/><el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/></el-select></el-form-item>
        <el-form-item label="巡查项目"><el-select v-model="editForm.subType" clearable><el-option label="燃气安全" value="gas"/><el-option label="消防通道" value="fire"/><el-option label="租户" value="tenant"/><el-option label="独居老人" value="solo"/></el-select></el-form-item>
        <el-form-item label="巡查结果"><el-select v-model="editForm.inspectResult" clearable><el-option label="合格" value="pass"/><el-option label="存在隐患" value="hazard"/><el-option label="已整改" value="rectified"/></el-select></el-form-item>
        <el-form-item label="隐患级别"><el-select v-model="editForm.hazardLevel" clearable><el-option label="一般" value="minor"/><el-option label="较大" value="major"/><el-option label="重大" value="critical"/></el-select></el-form-item>
        <el-form-item label="巡查地址"><el-input v-model="editForm.addressName"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="editVisible=false">取消</el-button><el-button type="primary" @click="submitEdit">确认提交</el-button></div>
    </el-dialog>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
  </div>
</template>

<script>
import { listTenant, getLog, updateLog } from '@/api/inspect/dashboard'
import { listCourtyard } from '@/api/inspect/dashboard'

export default {
  name: 'InspectTenant',
  data() {
    return {
      loading: false, total: 0, list: [], courtyards: [], detail: null, detailVisible: false,
      editVisible: false, editForm: {},
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, subType: null, tenantName: null, tenantPhone: null },
      resultMap: { pass: '合格', hazard: '存在隐患', rectified: '已整改' },
      levelMap: { minor: '一般', major: '较大', critical: '重大' }
    }
  },
  created() {
    // 从 URL 参数中获取人员类型（dashboard 跳转时传参）
    if (this.$route.query.subType) {
      this.queryParams.subType = this.$route.query.subType;
    }
    this.loadCourtyards();
    this.handleQuery();
  },
  methods: {
    loadCourtyards() { listCourtyard().then(res => { this.courtyards = res.data || []; }); },
    handleQuery() { this.loading = true; listTenant(this.queryParams).then(res => { this.list = res.rows; this.total = res.total; this.loading = false; }); },
    showDetail(row) { getLog(row.logId).then(res => { this.detail = res.data; this.detailVisible = true; }); },
    openEdit(row) { this.editForm = { ...row }; this.editVisible = true; },
    submitEdit() { updateLog(this.editForm).then(() => { this.editVisible = false; this.$message.success('订正成功'); this.handleQuery(); }); }
  }
}
</script>
