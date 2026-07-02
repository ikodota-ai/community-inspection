<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" size="small">
      <el-form-item label="网格"><el-select v-model="queryParams.courtyardId" clearable @change="handleQuery" placeholder="全部"><el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/></el-select></el-form-item>
      <el-form-item label="地址"><el-input v-model="queryParams.addressName" placeholder="搜索地址"/></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openAdd" icon="el-icon-plus">新增地址</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="网格" prop="courtyardName" width="120"/>
      <el-table-column label="地址" prop="addressName" min-width="250"/>
      <el-table-column label="类型" width="100"><template slot-scope="s">{{ typeMap[s.row.placeType]||s.row.placeType }}</template></el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="s">
          <el-button size="mini" @click="openEdit(s.row)">修改</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>

    <el-dialog :title="isEdit?'修改地址':'新增地址'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="所属网格"><el-select v-model="form.courtyardId"><el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/></el-select></el-form-item>
        <el-form-item label="地址全称"><el-input v-model="form.addressName"/></el-form-item>
        <el-form-item label="巡查类型"><el-select v-model="form.placeType"><el-option label="楼栋" value="building"/><el-option label="街巷" value="street"/><el-option label="公共场所" value="public"/><el-option label="商企" value="biz"/><el-option label="工地" value="site"/></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { listAddress, addAddress, updateAddress, delAddress } from '@/api/inspect/address'
import { listCourtyard } from '@/api/inspect/dashboard'

export default {
  name: 'InspectAddress',
  data() {
    return {
      loading: false, total: 0, list: [], courtyards: [], dialogVisible: false, isEdit: false,
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, addressName: null },
      form: { courtyardId: null, addressName: '', placeType: 'building' },
      typeMap: { building:'楼栋', street:'街巷', public:'公共场所', biz:'商企', site:'工地' }
    }
  },
  created() { this.loadCourtyards(); this.handleQuery(); },
  methods: {
    loadCourtyards() { listCourtyard().then(res => { this.courtyards = res.data || []; }); },
    handleQuery() { this.loading = true; listAddress(this.queryParams).then(res => { this.list = res.rows; this.total = res.total; this.loading = false; }); },
    openAdd() { this.isEdit = false; this.form = { courtyardId: null, addressName: '', placeType: 'building' }; this.dialogVisible = true; },
    openEdit(row) { this.isEdit = true; this.form = { ...row }; this.dialogVisible = true; },
    submitForm() {
      const fn = this.isEdit ? updateAddress : addAddress;
      fn(this.form).then(() => { this.dialogVisible = false; this.$message.success('操作成功'); this.handleQuery(); });
    },
    handleDelete(row) {
      this.$confirm('确定删除该地址?', '提示', { type: 'warning' }).then(() => {
        delAddress(row.addressId).then(() => { this.$message.success('删除成功'); this.handleQuery(); });
      });
    }
  }
}
</script>
